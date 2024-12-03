const vertexShaderSource = String.raw`
#version 100

precision mediump float;

attribute vec3 aVertexPosition;
attribute vec3 aVertexNormal;
varying vec3 vVertexNormal;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

mat3 transpose(mat3 matrix) {
      return mat3(
            vec3(matrix[0].x, matrix[1].x, matrix[2].x),
            vec3(matrix[0].y, matrix[1].y, matrix[2].y),
            vec3(matrix[0].z, matrix[1].z, matrix[2].z)
      );
}

float det(mat2 matrix) {
    return matrix[0].x * matrix[1].y - matrix[0].y * matrix[1].x;
}

mat3 inverse(mat3 matrix) {
    vec3 row0 = matrix[0];
    vec3 row1 = matrix[1];
    vec3 row2 = matrix[2];

    vec3 minors0 = vec3(
        det(mat2(row1.y, row1.z, row2.y, row2.z)),
        det(mat2(row1.z, row1.x, row2.z, row2.x)),
        det(mat2(row1.x, row1.y, row2.x, row2.y))
    );
    vec3 minors1 = vec3(
        det(mat2(row2.y, row2.z, row0.y, row0.z)),
        det(mat2(row2.z, row2.x, row0.z, row0.x)),
        det(mat2(row2.x, row2.y, row0.x, row0.y))
    );
    vec3 minors2 = vec3(
        det(mat2(row0.y, row0.z, row1.y, row1.z)),
        det(mat2(row0.z, row0.x, row1.z, row1.x)),
        det(mat2(row0.x, row0.y, row1.x, row1.y))
    );

    mat3 adj = transpose(mat3(minors0, minors1, minors2));

    return (1.0 / dot(row0, minors0)) * adj;
}

void main() {
   mat4 mvp = uProjection * uView * uModel ;
   gl_Position = mvp * vec4(aVertexPosition, 1.0);

   mat3 normalMatrix = transpose(inverse(mat3(uModel)));
   vVertexNormal = normalMatrix * aVertexNormal;
}`

const fragmentShaderSource = String.raw`
#version 100

precision mediump float;

varying vec3 vVertexNormal;

void main() {
   gl_FragColor = vec4(normalize(vVertexNormal), 1.0);
}`

const canvas = document.getElementById('mainCanvas')
const width = canvas.clientWidth * window.devicePixelRatio
const height = canvas.clientHeight * window.devicePixelRatio
canvas.width = width
canvas.height = height

const gl = canvas.getContext('webgl')
gl.viewport(0, 0, width, height)

const vertexShader = gl.createShader(gl.VERTEX_SHADER)
gl.shaderSource(vertexShader, vertexShaderSource)
gl.compileShader(vertexShader)
const fragmentShader = gl.createShader(gl.FRAGMENT_SHADER)
gl.shaderSource(fragmentShader, fragmentShaderSource)
gl.compileShader(fragmentShader)
const shaderProgram = gl.createProgram()
gl.attachShader(shaderProgram, vertexShader)
gl.attachShader(shaderProgram, fragmentShader)
gl.linkProgram(shaderProgram)

const aVertexPosition = gl.getAttribLocation(shaderProgram, 'aVertexPosition')
const aVertexNormal = gl.getAttribLocation(shaderProgram, 'aVertexNormal')
const uModel = gl.getUniformLocation(shaderProgram, 'uModel')
const uView = gl.getUniformLocation(shaderProgram, 'uView')
const uProjection = gl.getUniformLocation(shaderProgram, 'uProjection')

const vboRef = {
   value: null,
   vertexCount: 0
}

const uploadObjectFile = async () => {
   const objectFileContent = await new Promise(resolve => {
      const fileInput = document.createElement('input')
      fileInput.type = 'file'
      fileInput.accept = '.obj'
      fileInput.onchange = () => {
         const file = fileInput.files[0]
         if (!file) {
            resolve(undefined)
         }

         const reader = new FileReader()
         reader.onload = () => {
            resolve(reader.result)
         }
         reader.readAsText(file)
      }
      fileInput.click()
   })

   if (!objectFileContent) {
      return
   }

   const vertices = []
   const vertexNormals = []
   const vboData = []
   for (const originalLine of objectFileContent.split('\n')) {
      const line = originalLine.trim()
      if (line.startsWith("#")) {
         continue;
      }

      const parts = line.split(/\s+/)
      if (parts[0] === 'v') {
         vertices.push(parts.slice(1).map(parseFloat))
      } else if (parts[0] === 'vn') {
         vertexNormals.push(parts.slice(1).map(parseFloat))
      } else if (parts[0] === "f") {
         console.info(line)
         if (parts.length !== 4) {
            alert("只支持三角面!")
            return
         }

         for (let i = 1; i < 4; i++) {
            const [vertexIndex, normalIndex] = parts[i].split('//').map(s => parseInt(s))
            vboData.push(...vertices[vertexIndex - 1], ...vertexNormals[normalIndex - 1])
         }
      }
   }

   const vbo = gl.createBuffer()
   gl.bindBuffer(gl.ARRAY_BUFFER, vbo)
   gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vboData), gl.STATIC_DRAW)
   vboRef.value = vbo
   vboRef.vertexCount = vboData.length / 6
}

let frameCount = 0;
const renderFrame = () => {
   gl.clearColor(0.0, 0.0, 0.0, 1.0)
   gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)

   gl.useProgram(shaderProgram)
   gl.enable(gl.DEPTH_TEST)

   if (vboRef.value == null) {
      return
   }

   gl.bindBuffer(gl.ARRAY_BUFFER, vboRef.value)
   gl.enableVertexAttribArray(aVertexPosition)
   gl.vertexAttribPointer(aVertexPosition, 3, gl.FLOAT, false, 24, 0)
   gl.enableVertexAttribArray(aVertexNormal)
   gl.vertexAttribPointer(aVertexNormal, 3, gl.FLOAT, false, 24, 12)

   const p = glm.perspective(glm.radians(45), width / height, 0.1, 100)
   const m = glm.rotate(glm.mat4(), glm.radians(frameCount), glm.vec3(0, 1, 0))
   const v = glm.lookAt(glm.vec3(4, 3, 4), glm.vec3(0, 0, 0), glm.vec3(0, 1, 0))

   gl.uniformMatrix4fv(uModel, false, m.elements)
   gl.uniformMatrix4fv(uView, false, v.elements)
   gl.uniformMatrix4fv(uProjection, false, p.elements)

   gl.drawArrays(gl.TRIANGLES, 0, vboRef.vertexCount)
   frameCount += 1
}

const render = () => {
   renderFrame()
   requestAnimationFrame(render)
}

render()

