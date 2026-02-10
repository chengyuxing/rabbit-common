package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.*;

public interface IElementVisitor<R> {
    R visitIf(IfElement element);

    R visitForLoop(ForLoopElement element);

    R visitVarDefine(VarDefineElement element);

    R visitCheck(CheckElement element);

    R visitGuard(GuardElement element);

    R visitSwitch(SwitchElement element);

    R visitChoose(ChooseElement element);

    R visitPlainText(TextElement element);
}
