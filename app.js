const express = require('express');
const writtenNumber = require('written-number');

const app = express();
const puerto = 3000;

// Ruta exclusiva para el Paso 3 (ej. http://localhost:3000/paso3/25)
app.get('/paso3/:numero', (req, res) => {
  const numero = parseInt(req.params.numero, 10);

  let resultadoLetras = writtenNumber(numero, { lang: 'es' });
  
  resultadoLetras = resultadoLetras.charAt(0).toUpperCase() + resultadoLetras.slice(1);

  const html = `
    <html>
      <head>
        <meta charset="utf-8">
        <title>Paso 3 - Node.js NPM</title>
      </head>
      <body>
        <h2>Paso 3: Conversión de Número a Letras (Librería NPM)</h2>
        <p><strong>Número ingresado:</strong> ${numero}</p>
        <p><strong>Resultado con 'written-number':</strong> ${resultadoLetras}</p>
      </body>
    </html>
  `;
  
  res.send(html);
});

app.listen(puerto, () => {
  console.log(`Servidor listo para el Paso 3. Entra a http://localhost:${puerto}/paso3/2145321`);
});