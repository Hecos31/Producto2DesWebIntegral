use Mojolicious::Lite -signatures;
use SOAP::Lite;
use Mojo::URL;

sub consumir_soap ($numero) {
    my $client = SOAP::Lite
        ->proxy('https://www.dataaccess.com/webservicesserver/NumberConversion.wso')
        ->uri('http://www.dataaccess.com/webservicesserver/');

    my $method = SOAP::Data->name('NumberToWords')
        ->attr({xmlns => 'http://www.dataaccess.com/webservicesserver/'});
    my $param = SOAP::Data->name('ubiNum')->value($numero);

    my $response = $client->call($method => $param);

    return "Error SOAP" if $response->fault;

    my $resultado = $response->result;
    $resultado =~ s/^\s+|\s+$//g if defined $resultado;
    
    return $resultado;
}

# Ruta exclusiva para el Paso 2 (http://localhost:3000/paso2/25)
get '/paso2/:numero' => sub ($c) {
    my $numero = $c->param('numero');
    
    my $resultado_ingles = consumir_soap($numero);
    
    my $url = Mojo::URL->new('https://api.mymemory.translated.net/get');
    $url->query(q => $resultado_ingles, langpair => 'en|es');
    
    my $tx = $c->ua->get($url);
    my $resultado_espanol = "Error en la traducción";
    
    if (my $res = $tx->result) {
        if ($res->is_success) {
            $resultado_espanol = $res->json('/responseData/translatedText');
        }
    }
    
    $c->render(inline => <<~'HTML', numero => $numero, ingles => $resultado_ingles, espanol => $resultado_espanol);
      <html>
        <head>
          <meta charset="utf-8">
          <title>Paso 2 - Traducción Perl</title>
        </head>
        <body>
          <h2>Paso 2: Consumo SOAP y Traducción (Perl)</h2>
          <p><strong>Número enviado:</strong> <%= $numero %></p>
          <p><strong>Resultado de la función:</strong> <%= $ingles %></p>
          <p><strong>Resultado final traducido a español:</strong> <%= $espanol %></p>
        </body>
      </html>
    HTML
};

app->start;