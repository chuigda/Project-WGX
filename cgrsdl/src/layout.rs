use std::fmt::Display;

#[derive(Clone, Copy, Debug)]
pub enum LayoutMode {
    VertexBuffer,
    UniformSTD140,
    UniformSTD430,
    PushConstant
}

#[derive(Clone, Copy, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub enum CGType {
    Float,
    Vector2,
    Vector3,
    Vector4,
    Matrix4x4
}

impl CGType {
    pub fn java_type_enum(&self) -> &'static str {
        match self {
            CGType::Float => "Float",
            CGType::Vector2 => "Vector2",
            CGType::Vector3 => "Vector3",
            CGType::Vector4 => "Vector4",
            CGType::Matrix4x4 => "Matrix4x4"
        }
    }
}

impl Display for CGType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", match self {
            CGType::Float => "float",
            CGType::Vector2 => "Vector2",
            CGType::Vector3 => "Vector3",
            CGType::Vector4 => "Vector4",
            CGType::Matrix4x4 => "Matrix4x4"
        })
    }
}

impl CGType {
    pub fn size(&self) -> usize {
        match self {
            CGType::Float => 4,
            CGType::Vector2 => 8,
            CGType::Vector3 => 12,
            CGType::Vector4 => 16,
            CGType::Matrix4x4 => 64
        }
    }

    pub fn align(&self, mode: LayoutMode) -> usize {
        match mode {
            LayoutMode::VertexBuffer => 4,
            LayoutMode::UniformSTD140 => {
                let size = self.size();
                if size <= 16 {
                    size
                } else {
                    16
                }
            }
            LayoutMode::UniformSTD430 |
            LayoutMode::PushConstant => self.size()
        }
    }
}

#[derive(Clone, Debug)]
pub struct Field {
    pub name: String,
    pub ty: CGType,

    pub offset: usize
}

pub struct LayoutBuilder {
    pub mode: LayoutMode,

    pub fields: Vec<Field>,
    pub current_offset: usize
}

impl LayoutBuilder {
    pub fn new(mode: LayoutMode) -> Self {
        Self {
            mode,

            fields: Vec::new(),
            current_offset: 0
        }
    }

    pub fn add_field(&mut self, name: &str, ty: CGType) {
        let align = ty.align(self.mode);
        let offset = (self.current_offset + align - 1) / align * align;

        self.fields.push(Field {
            name: name.to_owned(),
            ty,

            offset
        });

        self.current_offset = offset + ty.size();
    }

    pub fn build(self) -> Vec<Field> {
        self.fields
    }
}
