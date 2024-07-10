/*
 * Copyright © 2024 Ben Petrillo. All rights reserved.
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

import dev.benpetrillo.elixir.types.ElixirException;
import dev.benpetrillo.elixir.utilities.Utilities;
import dev.benpetrillo.elixir.utilities.absolute.ElixirConstants;
import tech.xigam.express.Express;
import tech.xigam.express.Request;
import tech.xigam.express.Router;

import java.io.IOException;
import java.util.function.Consumer;

public final class APIHandler {

    public static void initialize() {
        final String address = ElixirConstants.API_ADDRESS;
        final int port = Integer.parseInt(ElixirConstants.API_PORT);
        final Consumer<Request> notFound = GeneralEndpoints::notFoundEndpoint;
        final Express express = Express.create(port, address).notFound(notFound);
        final Router router = new Router()
                .get("/", GeneralEndpoints::indexEndpoint)
                .get("/player", PlayerEndpoint::indexEndpoint)
                .get("/player/join", PlayerEndpoint::joinEndpoint)
                .get("/playlist", PlaylistEndpoint::indexEndpoint)
                .get("/queue", QueueEndpoint::indexEndpoint);
        try {
            express.router(router).listen();
        } catch (IOException exception) {
            Utilities.throwThrowable(new ElixirException().exception(exception));
        }
    }
}