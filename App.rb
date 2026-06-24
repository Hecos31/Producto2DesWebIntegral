require 'sinatra'
require 'savon'

# Ruta que recibe el número en la URL (ej. http://127.0.0.1:4567/paso1/10)
get '/paso1/:numero' do
  numero = params[:numero].to_i
  
  client = Savon.client(wsdl: 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL')
  
  response = client.call(:number_to_words, message: { "ubiNum" => numero })
  
  resultado_ingles = response.body[:number_to_words_response][:number_to_words_result].strip
  
  <<~HTML
    <html>
      <body>
        <h2>Consumo de Servicio SOAP Público</h2>
        <p><strong>Número enviado:</strong> #{numero}</p>
        <p><strong>Respuesta del servicio:</strong> #{resultado_ingles}</p>
      </body>
    </html>
  HTML
end