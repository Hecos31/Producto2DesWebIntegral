require 'sinatra'
require 'savon'
require 'net/http'
require 'uri'
require 'json'

# Esta función encapsula el consumo del servicio SOAP.
def consumir_soap(numero)
  client = Savon.client(wsdl: 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL')
  response = client.call(:number_to_words, message: { "ubiNum" => numero })
  
  # Retornamos el resultado limpio
  response.body[:number_to_words_response][:number_to_words_result].strip
end

# Ruta que recibe el número en la URL (ej. http://localhost:4567/paso2/25)
get '/paso2/:numero' do
  numero = params[:numero].to_i
  
  resultado_ingles = consumir_soap(numero)
  
  url_traduccion = URI("https://api.mymemory.translated.net/get?q=#{URI.encode_www_form_component(resultado_ingles)}&langpair=en|es")
  respuesta_http = Net::HTTP.get(url_traduccion)
  datos_traduccion = JSON.parse(respuesta_http)
  
  resultado_espanol = datos_traduccion['responseData']['translatedText']
  
  <<~HTML
    <html>
      <head>
        <meta charset="utf-8">
        <title>Paso 2 - Traducción</title>
      </head>
      <body>
        <h2>Paso 2: Consumo SOAP y Traducción</h2>
        <p><strong>Número enviado:</strong> #{numero}</p>
        <p><strong>Resultado de la función (Paso 1):</strong> #{resultado_ingles}</p>
        <p><strong>Resultado final traducido a español:</strong>#{resultado_espanol}</p>
      </body>
    </html>
  HTML
end