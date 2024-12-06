use std::{
    collections::HashMap,
    ffi::{c_char, CStr, CString},
    ptr::null_mut
};
use cgmath::Vector4;

#[repr(C)]
pub struct WGModelCollection {
    pub models: *mut WGMesh,
    pub model_count: u64
}

#[repr(C)]
pub struct WGMesh {
    pub model_name: *const c_char,
    pub vertices: *mut WGVertex,
    pub vertex_count: u64,
    pub indices: *mut u32,
    pub index_count: u64
}

#[repr(C)]
pub struct WGVertex {
    pub position: [f32; 3],
    pub normal: [f32; 3],
    pub tex_coords: [f32; 2],
    pub id: u32
}

pub extern "C" fn gltf_read(file: *const c_char) -> *mut WGModelCollection {
    let Ok(gltf) = easy_gltf::load(unsafe { CStr::from_ptr(file) }.to_str().unwrap()) else {
        return null_mut();
    };

    let mut next_id: u32 = 0;
    let mut models: Vec<WGMesh> = Vec::new();
    for scene in gltf {
        for model in scene.models {
            // not shared between models, so each model's each color will be assigned a unique id,
            // no matter if they are the same color
            let mut color_id_map: HashMap<Vector4<u16>, u32> = HashMap::new();

            // convert model name to CString, and then leak it to a raw pointer
            let model_name = model.mesh_name()
                .map(|name| CString::new(name).unwrap().into_raw())
                .unwrap_or(null_mut());

            let mut vertices: Vec<WGVertex> = Vec::new();
            for vertex in model.vertices() {
                let color_id = color_id_map.entry(vertex.color).or_insert_with(|| {
                    let id = next_id;
                    next_id += 1;
                    id
                });

                vertices.push(WGVertex {
                    position: vertex.position.into(),
                    normal: vertex.normal.into(),
                    tex_coords: vertex.tex_coords.into(),
                    id: *color_id
                });
            }
            vertices.shrink_to_fit();
            let vertex_count = vertices.len() as u64;
            let vertices_ref_leaked = Vec::leak(vertices);
            let vertices_ptr = vertices_ref_leaked.as_mut_ptr();

            if let Some(indices) = model.indices() {
                let mut indices_cloned = indices.clone();
                indices_cloned.shrink_to_fit();
                let index_count = indices_cloned.len() as u64;
                let indices_ref_leaked = Vec::leak(indices_cloned);
                let indices_ptr = indices_ref_leaked.as_mut_ptr();

                models.push(WGMesh {
                    model_name,
                    vertices: vertices_ptr,
                    vertex_count: vertex_count as u64,
                    indices: indices_ptr,
                    index_count: index_count as u64
                });
            } else {
                models.push(WGMesh {
                    model_name,
                    vertices: vertices_ptr,
                    vertex_count: vertex_count as u64,
                    indices: null_mut(),
                    index_count: 0
                });
            }
        }
    }

    models.shrink_to_fit();
    let model_count = models.len() as u64;
    let models_ref_leaked = Box::leak(models.into_boxed_slice()) as &mut [WGMesh];
    let models_ptr = models_ref_leaked.as_mut_ptr();

    Box::leak(Box::new(WGModelCollection {
        models: models_ptr,
        model_count
    }))
}

pub extern "C" fn gltf_free(model: *const WGModelCollection) {
    if model.is_null() {
        return;
    }

    let collection = unsafe { Box::from_raw(model as *mut WGModelCollection) };
    let models = unsafe { Vec::from_raw_parts(
        collection.models,
        collection.model_count as usize,
        collection.model_count as usize
    ) };
    for model in models {
        if !model.model_name.is_null() {
            let model_name = unsafe { CString::from_raw(model.model_name as *mut c_char) };
            drop(model_name);
        }
        if !model.vertices.is_null() {
            let vertices = unsafe { Vec::from_raw_parts(
                model.vertices,
                model.vertex_count as usize,
                model.vertex_count as usize
            ) };
            drop(vertices);
        }
        if !model.indices.is_null() {
            let indices = unsafe { Vec::from_raw_parts(
                model.indices,
                model.index_count as usize,
                model.index_count as usize
            ) };
            drop(indices);
        }
    }
    drop(collection);
}
