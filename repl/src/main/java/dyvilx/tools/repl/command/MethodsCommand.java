package dyvilx.tools.repl.command;

import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.util.MemberSorter;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.repl.DyvilREPL;
import dyvilx.tools.repl.lang.I18n;

import java.util.Arrays;
import java.util.List;

public class MethodsCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "methods";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "m" };
	}

	@Override
	public String getUsage()
	{
		return ":methods";
	}

	@Override
	public void execute(DyvilREPL repl, String args)
	{
		final List<IMethod> methodList = repl.getContext().getMethods();

		if (methodList.isEmpty())
		{
			repl.getOutput().println(I18n.get("command.methods.none"));
			return;
		}

		final IMethod[] methods = methodList.toArray(new IMethod[0]);
		Arrays.sort(methods, MemberSorter.METHOD_COMPARATOR);

		for (IMethod method : methods)
		{
			repl.getOutput().println(Util.methodSignatureToString(method, null));
		}
	}
}
