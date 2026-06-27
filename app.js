const express = require('express');
const soap = require('soap');

const app = express();
const puerto = 3000;

const urlWSDL = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL';

// Ruta exclusiva para el Paso 2 (ej. http://localhost:3000/paso2/25)
app.get('/paso2/:numero', async (req, res) => {
  const numero = parseInt(req.params.numero, 10);

  try {
    const client = await soap.createClientAsync(urlWSDL);
    const [resultadoSOAP] = await client.NumberToWordsAsync({ ubiNum: numero });
    
    let resultadoIngles = resultadoSOAP.NumberToWordsResult;
    if (resultadoIngles) {
      resultadoIngles = resultadoIngles.trim();
    }

    const urlTraduccion = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(resultadoIngles)}&langpair=en|es`;
    
    const respuestaHTTP = await fetch(urlTraduccion);
    const datosTraduccion = await respuestaHTTP.json();
    
    const resultadoEspanol = datosTraduccion.responseData.translatedText;

    const html = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>Paso 2 - Traducción Node.js</title>
        </head>
        <body>
          <h2>Paso 2: Consumo SOAP y Traducción (Node.js)</h2>
          <p><strong>Número enviado:</strong> ${numero}</p>
          <p><strong>Resultado de la función (Paso 1):</strong> ${resultadoIngles}</p>
          <p><strong>Resultado final traducido a español:</strong> ${resultadoEspanol}</p>
        </body>
      </html>
    `;
    
    res.send(html);

  } catch (error) {
    console.error(error);
    res.status(500).send("Ocurrió un error en el proceso: " + error.message);
  }
});

app.listen(puerto, () => {
  console.log(`Servidor listo para el Paso 2. Entra a http://localhost:${puerto}/paso2/25`);
});