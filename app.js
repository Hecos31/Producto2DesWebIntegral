const express = require('express');
const soap = require('soap');

const app = express();
const puerto = 3000;

const urlWSDL = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL';

// Ruta que recibe el número en la URL (ej. http://localhost:3000/paso1/25)
app.get('/paso1/:numero', (req, res) => {
  const numero = parseInt(req.params.numero, 10);

  const argumentos = { ubiNum: numero };

  soap.createClient(urlWSDL, (errorCreacion, client) => {
    if (errorCreacion) {
      return res.status(500).send("Error al crear el cliente SOAP: " + errorCreacion);
    }

    client.NumberToWords(argumentos, (errorLlamada, resultadoSOAP) => {
      if (errorLlamada) {
        return res.status(500).send("Error consumiendo el servicio SOAP: " + errorLlamada);
      }

      let resultadoIngles = resultadoSOAP.NumberToWordsResult;
      if (resultadoIngles) {
        resultadoIngles = resultadoIngles.trim();
      }

      const html = `
        <html>
          <head>
            <meta charset="utf-8">
            <title>Paso 1 - SOAP Node.js</title>
          </head>
          <body>
            <h2>Paso 1: Consumo SOAP (Node.js)</h2>
            <p><strong>Número enviado:</strong> ${numero}</p>
            <p><strong>Resultado en inglés:</strong> ${resultadoIngles}</p>
          </body>
        </html>
      `;
      
      res.send(html);
    });
  });
});

app.listen(puerto, () => {
  console.log(`Servidor levantado. Prueba entrando a http://localhost:${puerto}/paso1/25`);
});