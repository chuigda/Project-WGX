use std::{collections::HashMap, io::Write};

use cgmath::Vector4;

#[repr(C)]
struct MVertex {
    position: [f32; 3],
    normal: [f32; 3],
    uv: [f32; 2],
    id: u32,
}

fn main() {
    let args = std::env::args().collect::<Vec<String>>();
    if args.len() < 2 {
        eprintln!("usage: {} <glb file>", args[0]);
        return;
    }

    let file = &args[1];
    let export_mode = if args.len() == 3 {
        match args[2].as_str() {
            "obj" => "obj",
            "binary" => "binary",
            _ => {
                eprintln!("invalid export mode: {}", args[2]);
                return;
            }
        }
    } else {
        "binary"
    };

    let scenes = easy_gltf::load(file).unwrap();
    for scene in scenes {
        let mut current_color_id = 0;
        for model in scene.models {
            let model_name = model.mesh_name().unwrap_or("default");
            let output_file = format!(
                "{}.{}",
                model_name,
                if export_mode == "obj" {
                    "obj"
                } else {
                    "bin"
                }
            );

            let mut color_id_map: HashMap<Vector4<u16>, u32> = HashMap::new();
            let mut id_color_map: HashMap<u32, Vector4<u16>> = HashMap::new();
            let mut vertices: Vec<MVertex> = Vec::new();

            let mesh_vertices = model.vertices();
            if let Some(mesh_indices) = model.indices() {
                for yandeks in mesh_indices {
                    let mesh_vertex = &mesh_vertices[*yandeks as usize];
                    let position = mesh_vertex.position;
                    let normal = mesh_vertex.normal;
                    let uv = mesh_vertex.tex_coords;
                    let color = mesh_vertex.color;
                    let color_id = *color_id_map.entry(color).or_insert_with(|| {
                        let id = current_color_id;
                        id_color_map.insert(id, color);

                        current_color_id += 1;
                        id
                    });

                    vertices.push(MVertex {
                        position: [position.x, position.y, position.z],
                        normal: [normal.x, normal.y, normal.z],
                        uv: [uv.x, uv.y],
                        id: color_id,
                    });
                }
            }
            else {
                for mesh_vertex in mesh_vertices {
                    let position = mesh_vertex.position;
                    let normal = mesh_vertex.normal;
                    let uv = mesh_vertex.tex_coords;
                    let color = mesh_vertex.color;
                    let color_id = *color_id_map.entry(color).or_insert_with(|| {
                        let id = current_color_id;
                        current_color_id += 1;
                        id
                    });

                    vertices.push(MVertex {
                        position: [position.x, position.y, position.z],
                        normal: [normal.x, normal.y, normal.z],
                        uv: [uv.x, uv.y],
                        id: color_id,
                    });
                }
            }

            if export_mode == "obj" {
                let mut file = std::fs::File::create(output_file).unwrap();
                for vertex in &vertices {
                    let color = id_color_map.get(&vertex.id).unwrap();

                    writeln!(
                        file,
                        "v {} {} {} {} {} {}",
                        vertex.position[0], vertex.position[1], vertex.position[2],
                        color.x as f32 / 65535.0, color.y as f32 / 65535.0, color.z as f32 / 65535.0
                    ).unwrap();
                    writeln!(
                        file,
                        "vn {} {} {}",
                        vertex.normal[0], vertex.normal[1], vertex.normal[2]
                    )
                    .unwrap();
                    writeln!(file, "vt {} {}", vertex.uv[0], vertex.uv[1]).unwrap();
                }

                for i in (0..vertices.len()).step_by(3) {
                    writeln!(
                        file,
                        "f {}/{}/{} {}/{}/{} {}/{}/{}",
                        i + 1, i + 1, i + 1,
                        i + 2, i + 2, i + 2,
                        i + 3, i + 3, i + 3
                    )
                    .unwrap();
                }
            } else {
                let mut file = std::fs::File::create(output_file).unwrap();
                let vertices_bytes = unsafe {
                    std::slice::from_raw_parts(
                        vertices.as_ptr() as *const u8,
                        vertices.len() * std::mem::size_of::<MVertex>(),
                    )
                };
                let bytes_written = file.write(vertices_bytes).unwrap();
                assert_eq!(bytes_written, vertices_bytes.len());
            }
        }
    }
}
