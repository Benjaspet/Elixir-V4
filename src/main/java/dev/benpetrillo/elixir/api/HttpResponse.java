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

public class HttpResponse {
    public final StringBuilder html = new StringBuilder();
    public int statusCode = -1;
    
    public HttpResponse() {
        this.html.append("""
                <!DOCTYPE html>
                <html>
                """);
    }
    
    protected final HttpResponse title(String title) {
        html.append("<title>").append(title).append("</title>");
        return this;
    }
    
    protected final HttpResponse text(String text) {
        html.append("<p>").append(text).append("</p>");
        return this;
    }
    
    protected final void close() {
        html.append("</html>");
    }
    
    public static class Default extends HttpResponse { 
        public Default() {
            this.title("Elixir Music API")
                    .text("Welcome to the Elixir Music API!")
                    .close();
        }
    }

    public static class NotFound extends HttpResponse {
        public NotFound() {
            this.title("Error | 404 Not Found")
                    .text("The requested resource was not found.")
                    .close();
            this.statusCode = 404;
        }
    }
    
    public static class Success extends HttpResponse {
        public Success() {
            this.title("Success | 200 OK")
                    .text("The request was successful.")
                    .close();
            this.statusCode = 200;
        }
    }
}
