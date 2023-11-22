use std::error::Error;
use rsdl::{
    codegen::{CodeGenerator, Doc, CodeGeneratorFactory},
    parser::hir::{
        check_inline,
        RSDLType,
        AttrItem,
        TypeConstructor,
        SumType, extract_doc_strings
    },
    min_resolv::ResolveContext
};

use crate::layout::LayoutMode;

pub struct JavaGen();

impl JavaGen {
    fn type_to_string(&self, ctx: &ResolveContext, ty: &RSDLType) -> Result<String, Box<dyn Error>> {
        match ty {
            RSDLType::Identifier(ident) => {
                if let Some((_, rsdl_type, is_inline)) = ctx.known_types.get(ident) {
                    if *is_inline {
                        let Some(rsdl_type) = rsdl_type else {
                            return Err(format!("Unknown type: {}", ident).into());
                        };
                        self.type_to_string(ctx, rsdl_type)
                    } else {
                        Ok(ident.to_string())
                    }
                } else {
                    return Err(format!("Unknown type: {}", ident).into());
                }
            },
            RSDLType::Native(native) => if let Some(java_name) = native.get("typescript") {
                Ok(java_name.to_string())
            } else {
                Err(format!("Unknown native type: {:?}", native).into())
            },
            RSDLType::List(_) => Err("CG data structure generator does not support lists".into()),
            RSDLType::Record(_) => Err("CG data structure generator does not support records".into()),
        }
    }

    fn layout_mode(attr_list: &[AttrItem]) -> Result<LayoutMode, Box<dyn Error>> {
        for attr in attr_list {
            if let AttrItem::Identifier(ident) = attr {
                match ident.as_str() {
                    "vertex" => return Ok(LayoutMode::VertexBuffer),
                    "push_constant" => return Ok(LayoutMode::PushConstant),
                    _ => {}
                }
            }
            else if let AttrItem::CallAlike(fn_alike, args) = attr {
                if fn_alike == "uniform" {
                    if args.len() == 1 {
                        let AttrItem::Identifier(ident) = &args[0] else {
                            return Err("Invalid uniform layout attribute".into());
                        };

                        match ident.as_str() {
                            "std140" => return Ok(LayoutMode::UniformSTD140),
                            "std430" => return Ok(LayoutMode::UniformSTD430),
                            "vulkan" => return Ok(LayoutMode::UniformVulkan),
                            _ => {}
                        }
                    } else {
                        return Err("Invalid uniform layout attribute".into());
                    }
                }
            }
        }

        Err("No valid layout mode specified".into())
    }
}

impl CodeGenerator for JavaGen {
    fn generator_name(&self) -> &'static str {
        "Java CG data structure generator"
    }

    fn lang_ident(&self) -> &'static str {
        "java"
    }

    fn reserved_idents(&self) -> &[&'static str] {
        // https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
        &[
            "abstract",
            "assert",
            "boolean",
            "break",
            "byte",
            "case",
            "catch",
            "char",
            "class",
            "const",
            "continue",
            "default",
            "do",
            "double",
            "else",
            "enum",
            "extends",
            "final",
            "finally",
            "float",
            "for",
            "goto",
            "if",
            "implements",
            "import",
            "instanceof",
            "int",
            "interface",
            "long",
            "native",
            "new",
            "package",
            "private",
            "protected",
            "public",
            "return",
            "short",
            "static",
            "strictfp",
            "super",
            "switch",
            "synchronized",
            "this",
            "throw",
            "throws",
            "transient",
            "try",
            "void",
            "volatile",
            "while"
        ]
    }

    fn visit_namespace_begin(
        &mut self,
        _namespace: &str,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        Ok(())
    }

    fn visit_namespace_end(
        &mut self,
        _namespace: &str,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        Ok(())
    }

    fn visit_type_alias(
        &mut self,
        _ctx: &ResolveContext,
        attr: &[AttrItem],
        _alias_name: &str,
        _target_type: &RSDLType,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        if !check_inline(attr) {
            return Err("Java does not support type aliasing".into());
        }
        Ok(())
    }

    fn visit_simple_type(
        &mut self,
        _ctx: &ResolveContext,
        attr: &[AttrItem],
        type_ctor: &TypeConstructor,
        output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        let java_doc = extract_doc_strings(attr, "java_doc")?;
        if !java_doc.is_empty() {
            output.push_str("/**");
            for line in java_doc {
                output.push_string(format!(" * {}", line));
            }
            output.push_str(" */");
        }

        output.push_string(format!("public class {} implements Vertex, IntoBytes {{", type_ctor.name));
        output.push_str("}");

        Ok(())
    }

    fn visit_sum_type(
        &mut self,
        _ctx: &ResolveContext,
        _attr: &[AttrItem],
        _sum_type: &SumType,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        Err("Java does not support type aliasing".into())
    }

    fn visit_sum_type_ctor(
        &mut self,
        _ctx: &ResolveContext,
        _attr: &[AttrItem],
        _ctor: &TypeConstructor,
        _sum_type: &SumType,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        unreachable!()
    }

    fn visit_sum_type_scalar_variant(
        &mut self,
        _ctx: &ResolveContext,
        _attr: &[AttrItem],
        _variant_name: &str,
        _sum_type: &SumType,
        _output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        unreachable!()
    }

    fn pre_visit(
        &mut self,
        ctx: &ResolveContext,
        output: &mut Doc
    ) -> Result<(), Box<dyn Error>> {
        let mut has_package_name = false;

        for attr in &ctx.global_attr {
            match attr {
                AttrItem::Assignment(attr_name, value) if attr_name == "package" => {
                    let AttrItem::String(package_name) = value.as_ref() else {
                        return Err("Invalid package name attribute".into());
                    };

                    if has_package_name {
                        return Err("Multiple package name attributes".into());
                    }

                    has_package_name = true;
                    output.push_string(format!("package {package_name}"));
                    output.push_empty_line();
                }
                _ => {}
            }
        }

        Ok(())
    }
}

pub struct JavaGenFactory();

impl CodeGeneratorFactory for JavaGenFactory {
    fn generator_name(&self) -> &'static str {
        JavaGen().generator_name()
    }

    fn lang_ident(&self) -> &'static str {
        JavaGen().lang_ident()
    }

    fn create(&self) -> Box<dyn CodeGenerator> {
        Box::new(JavaGen())
    }
}
