package com.vincentcodes.webserver.dispatcher;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.helper.Filterable;

public interface IRequestsFilter extends Filterable<HttpRequest> {
}