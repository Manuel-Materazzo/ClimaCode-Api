package com.manu.forecaster.service;

import com.manu.forecaster.dto.TileRapresentation;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Scope("singleton")
public class SpelService {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext context = new StandardEvaluationContext();

    /**
     * Replaces the standard xyz and the SpEL evaluated version of the provided templates on the source string.
     *
     * @param source    source string where the replacing happens
     * @param templates SpEL templates to be evaluated and replaced
     * @param tile      source tile for the xyz replace, if null skips only the xyz replace.
     * @return
     */
    public String applyTemplates(String source, Map<String, String> templates, TileRapresentation tile) {

        // replace basic tile xyz values
        if (tile != null) {
            source = source.replace("\\{x}", String.valueOf(tile.getX()));
            source = source.replace("\\{y}", String.valueOf(tile.getY()));
            source = source.replace("\\{z}", String.valueOf(tile.getZ()));
        }

        // replace every defined template with its parsed SpEL expression
        for (var templateEntry : templates.entrySet()) {
            String searchString = String.format("\\{%s}", templateEntry.getKey());
            source = source.replace(searchString, parseSpel(templateEntry.getValue()));
        }

        return source;
    }

    /**
     * Evaluates the given Spel expression
     *
     * @param expression
     * @return
     */
    public String parseSpel(String expression) {
        Expression exp = parser.parseExpression(expression);
        return exp.getValue(context, String.class);
    }
}
