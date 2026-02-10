package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.CleanStringJoiner;
import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;
import com.github.chengyuxing.common.script.ast.ScriptAst;
import com.github.chengyuxing.common.script.exception.CheckViolationException;
import com.github.chengyuxing.common.script.exception.GuardViolationException;
import com.github.chengyuxing.common.script.lang.Comparators;
import com.github.chengyuxing.common.script.lang.ForContextProperty;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.ValueUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;

public class RabbitScriptEvaluator implements IElementVisitor<Void> {
    private final CleanStringJoiner sql;
    private final EvalContext ctx;
    private long varSeq = 0;
    private final Map<String, Object> usedVars = new HashMap<>();

    public RabbitScriptEvaluator(@NotNull EvalContext ctx) {
        this.ctx = ctx;
        this.sql = new CleanStringJoiner(NEW_LINE);
    }

    public EvalResult execute(@NotNull ScriptAst script) {
        for (IElement element : script.getElements()) {
            element.accept(this);
        }
        return new EvalResult(sql.toString(), usedVars);
    }

    private void defineVars(String name, Object value) {
        VarMeta varMeta = new VarMeta(name, value, varSeq++);
        ctx.bindScope(name, varMeta);
    }

    private void visitBlock(List<IElement> elements, @Nullable Runnable beforeVisit) {
        ctx.pushScope();
        if (beforeVisit != null) {
            beforeVisit.run();
        }
        for (IElement e : elements) {
            e.accept(this);
        }
        ctx.popScope();
    }

    @Override
    public Void visitIf(IfElement element) {
        boolean ok = element.getExpr().eval(ctx);

        List<IElement> block = ok
                ? element.getThenBlock()
                : element.getElseBlock();
        visitBlock(block, null);
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopElement element) {
        Object obj = element.getValueExpr().eval(ctx);
        Iterator<?> it = ValueUtils.asIterable(obj).iterator();
        if (!it.hasNext()) {
            return null;
        }
        // if empty loop body just do nothing.
        List<IElement> loopBlock = element.getLoopBlock();
        if (loopBlock.isEmpty()) {
            return null;
        }
        for (int i = 0; it.hasNext(); i++) {
            int index = i;
            visitBlock(loopBlock, () -> {
                defineVars(element.getItemName(), it.next());

                String indexAlias = element.getContextPropertyAlias(ForContextProperty.index);
                String firstAlias = element.getContextPropertyAlias(ForContextProperty.first);
                String lastAlias = element.getContextPropertyAlias(ForContextProperty.last);
                String oddAlias = element.getContextPropertyAlias(ForContextProperty.odd);
                String evenAlias = element.getContextPropertyAlias(ForContextProperty.even);

                if (indexAlias != null) defineVars(indexAlias, index);
                if (firstAlias != null) defineVars(firstAlias, index == 0);
                if (lastAlias != null) defineVars(lastAlias, !it.hasNext());
                if (oddAlias != null) defineVars(oddAlias, (index & 1) == 1);
                if (evenAlias != null) defineVars(evenAlias, (index & 1) == 0);
            });
        }
        return null;
    }

    @Override
    public Void visitVarDefine(VarDefineElement element) {
        Object value = element.getValueExpr().eval(ctx);
        defineVars(element.getName(), value);
        return null;
    }

    @Override
    public Void visitCheck(CheckElement element) {
        boolean ok = element.getExpr().eval(ctx);
        if (ok) {
            throw new CheckViolationException(element.getMessage());
        }
        return null;
    }

    @Override
    public Void visitGuard(GuardElement element) {
        boolean ok = element.getExpr().eval(ctx);
        if (ok) {
            visitBlock(element.getThenBlock(), null);
        } else {
            throw new GuardViolationException(element.getMessage());
        }
        return null;
    }

    @Override
    public Void visitSwitch(SwitchElement element) {
        Object switchValue = element.getValueExpr().eval(ctx);
        for (SwitchCaseBranchElement caseBranch : element.getCaseBranches()) {
            for (ValueExpr expr : caseBranch.getValueExpr()) {
                Object caseValue = expr.eval(ctx);
                if (Comparators.compare(switchValue, "=", caseValue)) {
                    visitBlock(caseBranch.getThenBlock(), null);
                    return null;
                }
            }
        }
        visitBlock(element.getDefaultBranch().getThenBlock(), null);
        return null;
    }

    @Override
    public Void visitChoose(ChooseElement element) {
        for (ChooseWhenBranchElement when : element.getWhenBranches()) {
            boolean ok = when.getExpr().eval(ctx);
            if (ok) {
                visitBlock(when.getThenBlock(), null);
                return null;
            }
        }
        visitBlock(element.getDefaultBranch().getThenBlock(), null);
        return null;
    }

    @Override
    public Void visitPlainText(TextElement element) {
        String text = element.getText();
        Pair<String, Map<String, Object>> result = ctx.formatScopePlainText(text);
        usedVars.putAll(result.getItem2());
        sql.add(result.getItem1());
        return null;
    }
}
