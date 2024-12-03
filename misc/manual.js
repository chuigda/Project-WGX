const normalPass_vertexShaderSource = String.raw`
#version 100

precision mediump float;

attribute vec3 aVertexPosition;
attribute vec3 aVertexColor;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

varying vec3 vVertexColor;

void main() {
   mat4 mvp = uProjection * uView * uModel;
   vVertexColor = aVertexColor;
   gl_Position = mvp * vec4(aVertexPosition, 1.0);
}`

const normalPass_fragmentShaderSource = String.raw`
#version 100

precision mediump float;

varying vec3 vVertexColor;

void main() {
   gl_FragColor = vec4(vVertexColor, 1.0);
}`

const colorPassCopy_vertexShaderSource = String.raw`
#version 100

precision mediump float;

attribute vec2 aVertexPosition;

void main() {
   gl_Position = vec4(aVertexPosition, 0.0, 1.0);
}
`

const colorPassCopy_fragmentShaderSource = String.raw`
#version 100

precision mediump float;

uniform sampler2D uNormalTexture;
uniform vec2 uViewportSize;

void main() {
   vec2 uv = gl_FragCoord.xy / uViewportSize;
   vec3 normal = texture2D(uNormalTexture, uv).rgb;
   gl_FragColor = vec4(normal, 1.0);
}
`

const colorPass_vertexShaderSource = String.raw`
#version 100

precision mediump float;

attribute vec2 aVertexPosition;

void main() {
   gl_Position = vec4(aVertexPosition, 0.0, 1.0);
}
`

const colorPass_fragmentShaderSource = String.raw`
#version 100

precision mediump float;

uniform sampler2D uNormalTexture;
uniform vec2 uViewportSize;

const mat3 sobelX = mat3(
   vec3(1.0, 2.0, 1.0),
   vec3(0.0, 0.0, 0.0),
   vec3(-1.0, -2.0, -1.0)
);

const mat3 sobelY = mat3(
   vec3(1.0, 0.0, -1.0),
   vec3(2.0, 0.0, -2.0),
   vec3(1.0, 0.0, -1.0)
);

bool closeToZero(vec3 v) {
   return v.x < 0.0001 && v.y < 0.0001 && v.z < 0.0001;
}

void main() {
   float u = (gl_FragCoord.x - uViewportSize.x / 2.0) / uViewportSize.x - 0.5;
   float v = gl_FragCoord.y / uViewportSize.y;
   vec2 uv = vec2(u, v);
   vec2 offset = vec2(1.0 / uViewportSize.x, 1.0 / uViewportSize.y);

   vec3 normal = texture2D(uNormalTexture, uv).rgb;
   if (closeToZero(normal)) {
      gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
      return;
   }

   vec3 n = texture2D(uNormalTexture, uv + vec2(0.0, -offset.y)).rgb;
   vec3 s = texture2D(uNormalTexture, uv + vec2(0.0, offset.y)).rgb;
   vec3 e = texture2D(uNormalTexture, uv + vec2(offset.x, 0.0)).rgb;
   vec3 w = texture2D(uNormalTexture, uv + vec2(-offset.x, 0.0)).rgb;
   vec3 nw = texture2D(uNormalTexture, uv + vec2(-offset.x, -offset.y)).rgb;
   vec3 ne = texture2D(uNormalTexture, uv + vec2(offset.x, -offset.y)).rgb;
   vec3 sw = texture2D(uNormalTexture, uv + vec2(-offset.x, offset.y)).rgb;
   vec3 se = texture2D(uNormalTexture, uv + vec2(offset.x, offset.y)).rgb;

   int count = 0;
   if (closeToZero(n)) { count += 1; }
   if (closeToZero(s)) { count += 1; }
   if (closeToZero(e)) { count += 1; }
   if (closeToZero(w)) { count += 1; }
   if (closeToZero(nw)) { count += 1; }
   if (closeToZero(ne)) { count += 1; }
   if (closeToZero(sw)) { count += 1; }
   if (closeToZero(se)) { count += 1; }
   if (count >= 3) {
      gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);
      return;
   }

   mat3 surrounding = mat3(
      vec3(length(nw - normal), length(n - normal), length(ne - normal)),
      vec3(length(w - normal), 0.0, length(e - normal)),
      vec3(length(sw - normal), length(s - normal), length(se - normal))
   );

   float edgeX = (dot(sobelX[0], surrounding[0]) + dot(sobelX[1], surrounding[1]) + dot(sobelX[2], surrounding[2])) / 9.0;
   float edgeY = (dot(sobelY[0], surrounding[0]) + dot(sobelY[1], surrounding[1]) + dot(sobelY[2], surrounding[2])) / 9.0;
   float edge = sqrt(edgeX * edgeX + edgeY * edgeY);

   if (edge > 0.15) {
      gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
   } else {
      gl_FragColor = vec4(edge * 2.0, 0.0, 0.0, 1.0);
   }
}
`

const canvas = document.getElementById('mainCanvas')
const width = canvas.clientWidth * window.devicePixelRatio
const height = canvas.clientHeight * window.devicePixelRatio
canvas.width = width
canvas.height = height

const gl = canvas.getContext('webgl')
gl.viewport(0, 0, width, height)

const colorAttachment = gl.createTexture()
gl.bindTexture(gl.TEXTURE_2D, colorAttachment)
gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, width / 2, height, 0, gl.RGBA, gl.UNSIGNED_BYTE, null)
gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST)
gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST)
gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE)
gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE)
const framebuffer = gl.createFramebuffer()
gl.bindFramebuffer(gl.FRAMEBUFFER, framebuffer)
gl.framebufferTexture2D(gl.FRAMEBUFFER, gl.COLOR_ATTACHMENT0, gl.TEXTURE_2D, colorAttachment, 0)
const depthAttachment = gl.createRenderbuffer()
gl.bindRenderbuffer(gl.RENDERBUFFER, depthAttachment)
gl.renderbufferStorage(gl.RENDERBUFFER, gl.DEPTH_COMPONENT16, width / 2, height)
gl.framebufferRenderbuffer(gl.FRAMEBUFFER, gl.DEPTH_ATTACHMENT, gl.RENDERBUFFER, depthAttachment)
gl.bindFramebuffer(gl.FRAMEBUFFER, null)

const normalPass_VS = gl.createShader(gl.VERTEX_SHADER)
gl.shaderSource(normalPass_VS, normalPass_vertexShaderSource)
gl.compileShader(normalPass_VS)
const normalPass_FS = gl.createShader(gl.FRAGMENT_SHADER)
gl.shaderSource(normalPass_FS, normalPass_fragmentShaderSource)
gl.compileShader(normalPass_FS)
const normalPass_shaderProgram = gl.createProgram()
gl.attachShader(normalPass_shaderProgram, normalPass_VS)
gl.attachShader(normalPass_shaderProgram, normalPass_FS)
gl.linkProgram(normalPass_shaderProgram)

const normalPass_shaderProgram_Info = {
   aVertexPosition: gl.getAttribLocation(normalPass_shaderProgram, 'aVertexPosition'),
   aVertexColor: gl.getAttribLocation(normalPass_shaderProgram, 'aVertexColor'),
   uModel: gl.getUniformLocation(normalPass_shaderProgram, 'uModel'),
   uView: gl.getUniformLocation(normalPass_shaderProgram, 'uView'),
   uProjection: gl.getUniformLocation(normalPass_shaderProgram, 'uProjection')
}

const colorPassCopy_VS = gl.createShader(gl.VERTEX_SHADER)
gl.shaderSource(colorPassCopy_VS, colorPassCopy_vertexShaderSource)
gl.compileShader(colorPassCopy_VS)
const colorPassCopy_FS = gl.createShader(gl.FRAGMENT_SHADER)
gl.shaderSource(colorPassCopy_FS, colorPassCopy_fragmentShaderSource)
gl.compileShader(colorPassCopy_FS)
const colorPassCopy_shaderProgram = gl.createProgram()
gl.attachShader(colorPassCopy_shaderProgram, colorPassCopy_VS)
gl.attachShader(colorPassCopy_shaderProgram, colorPassCopy_FS)
gl.linkProgram(colorPassCopy_shaderProgram)

const colorPassCopy_shaderProgram_Info = {
   aVertexPosition: gl.getAttribLocation(colorPassCopy_shaderProgram, 'aVertexPosition'),
   uNormalTexture: gl.getUniformLocation(colorPassCopy_shaderProgram, 'uNormalTexture'),
   uViewportSize: gl.getUniformLocation(colorPassCopy_shaderProgram, 'uViewportSize')
}

const colorPass_VS = gl.createShader(gl.VERTEX_SHADER)
gl.shaderSource(colorPass_VS, colorPass_vertexShaderSource)
gl.compileShader(colorPass_VS)
const colorPass_FS = gl.createShader(gl.FRAGMENT_SHADER)
gl.shaderSource(colorPass_FS, colorPass_fragmentShaderSource)
gl.compileShader(colorPass_FS)
const colorPass_shaderProgram = gl.createProgram()
gl.attachShader(colorPass_shaderProgram, colorPass_VS)
gl.attachShader(colorPass_shaderProgram, colorPass_FS)
gl.linkProgram(colorPass_shaderProgram)

const colorPass_shaderProgram_Info = {
   aVertexPosition: gl.getAttribLocation(colorPass_shaderProgram, 'aVertexPosition'),
   uNormalTexture: gl.getUniformLocation(colorPass_shaderProgram, 'uNormalTexture'),
   uViewportSize: gl.getUniformLocation(colorPass_shaderProgram, 'uViewportSize')
}

// use two triangles to cover the whole screen
const colorPassVertices_left = [
   -1, -1,
   0, -1,
   -1, 1,
   -1, 1,
   0, -1,
   0, 1
]
const colorPassVBO_left = gl.createBuffer()
gl.bindBuffer(gl.ARRAY_BUFFER, colorPassVBO_left)
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colorPassVertices_left), gl.STATIC_DRAW)

const colorPassVertices_right = [
   0, -1,
   1, -1,
   0, 1,
   0, 1,
   1, -1,
   1, 1
]
const colorPassVBO_right = gl.createBuffer()
gl.bindBuffer(gl.ARRAY_BUFFER, colorPassVBO_right)
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(colorPassVertices_right), gl.STATIC_DRAW)

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
   const vboData = []
   for (const originalLine of objectFileContent.split('\n')) {
      const line = originalLine.trim()
      if (line.startsWith("#")) {
         continue;
      }

      const parts = line.split(/\s+/)
      if (parts[0] === 'v') {
         vertices.push(parts.slice(1).map(parseFloat))
      } else if (parts[0] === "f") {
         if (parts.length !== 4) {
            alert("只支持三角面!")
            return
         }

         for (let i = 1; i < 4; i++) {
            const vertexIndex = parseInt(parts[i].split('/')[0])
            vboData.push(...vertices[vertexIndex - 1])
         }
      }
   }

   const vbo = gl.createBuffer()
   gl.bindBuffer(gl.ARRAY_BUFFER, vbo)
   gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vboData), gl.STATIC_DRAW)
   vboRef.value = vbo
   vboRef.vertexCount = vboData.length / 6
}

const renderFrame = () => {
   gl.clearColor(0.0, 0.0, 0.0, 1.0)

   if (vboRef.value != null) {
      gl.bindFramebuffer(gl.FRAMEBUFFER, framebuffer)
      gl.viewport(0, 0, width / 2, height)

      gl.useProgram(normalPass_shaderProgram)
      gl.enable(gl.DEPTH_TEST)
      gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT)
      gl.bindBuffer(gl.ARRAY_BUFFER, vboRef.value)
      gl.enableVertexAttribArray(normalPass_shaderProgram_Info.aVertexPosition)
      gl.vertexAttribPointer(normalPass_shaderProgram_Info.aVertexPosition, 3, gl.FLOAT, false, 24, 0)
      gl.enableVertexAttribArray(normalPass_shaderProgram_Info.aVertexColor)
      gl.vertexAttribPointer(normalPass_shaderProgram_Info.aVertexColor, 3, gl.FLOAT, false, 24, 12)

      const scroll = parseFloat(document.getElementById("rotate").value)

      const p = glm.perspective(glm.radians(45), 0.5 * width / height, 0.1, 100)
      const m = glm.rotate(glm.mat4(), glm.radians(scroll), glm.vec3(0, 1, 0))
      const v = glm.lookAt(glm.vec3(0.5, 0.5, 0.5), glm.vec3(0, 0, 0), glm.vec3(0, 1, 0))

      gl.uniformMatrix4fv(normalPass_shaderProgram_Info.uModel, false, m.elements)
      gl.uniformMatrix4fv(normalPass_shaderProgram_Info.uView, false, v.elements)
      gl.uniformMatrix4fv(normalPass_shaderProgram_Info.uProjection, false, p.elements)
      gl.drawArrays(gl.TRIANGLES, 0, vboRef.vertexCount)
   }
   gl.bindFramebuffer(gl.FRAMEBUFFER, null)
   gl.disable(gl.DEPTH_TEST)

   gl.viewport(0, 0, width, height)
   gl.clear(gl.COLOR_BUFFER_BIT)
   gl.useProgram(colorPassCopy_shaderProgram)
   gl.bindBuffer(gl.ARRAY_BUFFER, colorPassVBO_left)
   gl.enableVertexAttribArray(colorPassCopy_shaderProgram_Info.aVertexPosition)
   gl.vertexAttribPointer(colorPassCopy_shaderProgram_Info.aVertexPosition, 2, gl.FLOAT, false, 0, 0)
   gl.activeTexture(gl.TEXTURE0)
   gl.bindTexture(gl.TEXTURE_2D, colorAttachment)
   gl.uniform1i(colorPassCopy_shaderProgram_Info.uNormalTexture, 0)
   gl.uniform2f(colorPassCopy_shaderProgram_Info.uViewportSize, width / 2, height)
   gl.drawArrays(gl.TRIANGLES, 0, 6)

   gl.useProgram(colorPass_shaderProgram)
   gl.bindBuffer(gl.ARRAY_BUFFER, colorPassVBO_right)
   gl.enableVertexAttribArray(colorPass_shaderProgram_Info.aVertexPosition)
   gl.vertexAttribPointer(colorPass_shaderProgram_Info.aVertexPosition, 2, gl.FLOAT, false, 0, 0)
   gl.activeTexture(gl.TEXTURE0)
   gl.bindTexture(gl.TEXTURE_2D, colorAttachment)
   gl.uniform1i(colorPass_shaderProgram_Info.uNormalTexture, 0)
   gl.uniform2f(colorPass_shaderProgram_Info.uViewportSize, width / 2, height)
   gl.drawArrays(gl.TRIANGLES, 0, 6)
}

const render = () => {
   renderFrame()
   requestAnimationFrame(render)
}

render()

