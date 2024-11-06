package com.jindi.infra.trace.model;

import java.util.HashMap;

/**
 * Adds context to a span, for search, viewing and analysis.  For example, a key \&quot;your_app.version\&quot; would let you lookup traces by version. A tag \&quot;sql.query\&quot; isn&#39;t searchable, but it can help in debugging when viewing a trace.
 */
public class Tags extends HashMap<String, String> {
}