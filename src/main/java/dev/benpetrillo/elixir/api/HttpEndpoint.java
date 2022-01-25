/*
 * Copyright © 2022 Ben Petrillo. All rights reserved.
 *
 * Project licensed under the MIT License: https://www.mit.edu/~amini/LICENSE.md
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * All portions of this software are available for public use, provided that
 * credit is given to the original author(s).
 */

package dev.benpetrillo.elixir.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpEndpoint implements HttpHandler {
    protected final Map<String, String> arguments = new HashMap<>();
    protected int statusCode = 200;
    protected HttpExchange httpExchange;
    
    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        this.httpExchange = exchange; this.parseArguments();
        
        switch (exchange.getRequestMethod()) {
            default -> respond(new HttpResponse.Default().html.toString());
            case "GET" -> this.get();
            case "POST" -> this.post();
            case "PUT" -> this.put();
            case "DELETE" -> this.delete();
        }
    }
    
    private void parseArguments() {
        var requestUri = this.httpExchange.getRequestURI().toString();
        
        try {
            var args = requestUri.split("\\?")[1];
            var argumentPairs = args.split("&");

            // Parse the first argument.
            this.arguments.put(args.split("=")[0].strip(), args.split("=")[1].strip());
            // Parse the rest of the arguments.
            for (var pair : argumentPairs) {
                this.arguments.put(pair.split("=")[0].strip(), pair.split("=")[1].strip());
            }
        } catch (IndexOutOfBoundsException ignored) { }
    }
    
    protected final HttpEndpoint header(String header, String value) {
        this.httpExchange.getResponseHeaders().add(header, value); return this;
    }
    
    protected final void respond(String response) throws IOException {
        OutputStream output = this.httpExchange.getResponseBody();;
        
        this.httpExchange.sendResponseHeaders(this.statusCode, response.length());
        output.write(response.getBytes()); output.flush(); output.close();
    }
    
    protected final void respond(HttpResponse response) throws IOException {
        if(response.statusCode != -1)
            this.statusCode = response.statusCode;
        this.respond(String.valueOf(response.html));
    }
    
    public void get() throws IOException { this.respond(new HttpResponse.Default()); }
    public void post() throws IOException { this.respond(new HttpResponse.Default()); }
    public void put() throws IOException { this.respond(new HttpResponse.Default()); }
    public void delete() throws IOException { this.respond(new HttpResponse.Default()); }
}
