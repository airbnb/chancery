package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

public class ObjectKeyEvaluator {
    private final CompiledTemplate compiledTemplate;

    ObjectKeyEvaluator(String template) {
        compiledTemplate = TemplateCompiler.compileTemplate(template);
    }

    public final String getPath(CallbackPayload payload) {
        return (String) TemplateRuntime.execute(compiledTemplate, payload);
    }
}
