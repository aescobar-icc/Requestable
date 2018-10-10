package beauty.requestable.core.interfaces;

import java.util.List;

import beauty.requestable.core.url.UrlPattern;

public interface MultiUrl {
	List<UrlPattern> getUrlPatterns();
}
