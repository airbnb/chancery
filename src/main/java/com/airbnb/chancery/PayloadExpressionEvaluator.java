package com.airbnb.chancery;

import com.airbnb.chancery.model.CallbackPayload;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.mvel2.ParserContext;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import javax.annotation.Nonnull;

public class PayloadExpressionEvaluator {
    private final CompiledTemplate compiledTemplate;

    PayloadExpressionEvaluator(@Nonnull String template) {
        if (template == null) {
            System.err.println("wuuut");
        }
        final ParserContext parserContext = new ParserContext();
        parserContext.addImport("iso", ISODateTimeFormat.class);
        parserContext.addImport("dtf", DateTimeFormat.class);
        compiledTemplate = TemplateCompiler.compileTemplate(template, parserContext);
    }

    public final String evaluateForPayload(@Nonnull CallbackPayload payload) {
        return (String) TemplateRuntime.execute(compiledTemplate, payload);
    }
}
