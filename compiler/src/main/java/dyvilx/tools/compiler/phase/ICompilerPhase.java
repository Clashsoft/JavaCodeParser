package dyvilx.tools.compiler.phase;

import dyvil.annotation.internal.NonNull;
import dyvil.io.Files;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.config.CompilerConfig;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.token.IdentifierToken;

public interface ICompilerPhase extends Comparable<ICompilerPhase>
{
	/**
	 * Recursively deletes all files in the output directory.
	 *
	 * @see DyvilCompiler#clean()
	 */
	ICompilerPhase CLEAN = new CompilerPhase(0, "CLEAN", DyvilCompiler::clean);

	/**
	 * Splits the input file into {@link IdentifierToken Tokens} using {@link DyvilLexer}.
	 */
	ICompilerPhase TOKENIZE = new SequentialCompilerPhase(10, "TOKENIZE", ICompilationUnit::tokenize);

	/**
	 * Parses the list of tokens generated by {@link #TOKENIZE}.
	 */
	ICompilerPhase PARSE = new SequentialCompilerPhase(20, "PARSE", ICompilationUnit::parse);

	/**
	 * Prints the AST.
	 */
	ICompilerPhase PRINT = new PrintPhase(PARSE);

	/**
	 * Saves the formatted AST to the input file
	 */
	ICompilerPhase FORMAT = new SequentialCompilerPhase(40, "FORMAT", unit -> Files.tryWriteText(
		unit.getFileSource().file(), unit.toString()));

	ICompilerPhase RESOLVE_HEADERS = new ResolveHeaderPhase(45);

	/**
	 * Resolves packages, classes and types.
	 */
	ICompilerPhase RESOLVE_TYPES = new ResolveTypesPhase(50);

	/**
	 * Resolves methods and field names.
	 */
	ICompilerPhase RESOLVE = new SequentialCompilerPhase(60, "RESOLVE", ICompilationUnit::resolve);

	/**
	 * Resolves other things such as lambda expressions or annotations and checks types.
	 */
	ICompilerPhase CHECK_TYPES = new SequentialCompilerPhase(70, "CHECK_TYPES", ICompilationUnit::checkTypes);

	/**
	 * Checks for semantical errors. The general contract of this method is that it should not be mandatory for a
	 * compilation to succeed (given that everything is correct). Thus, it should not do any more linking or resolving.
	 */
	ICompilerPhase CHECK = new SequentialCompilerPhase(80, "CHECK", ICompilationUnit::check);

	/**
	 * Folds constants.
	 */
	ICompilerPhase FOLD_CONSTANTS = new FoldConstantPhase(90);

	/**
	 * Performs the final cleanup on the AST. This also includes special registrations such as adding Lambda Expressions
	 * to the list of compilable elements in classes.
	 */
	ICompilerPhase CLEANUP = new SequentialCompilerPhase(100, "CLEANUP", ICompilationUnit::cleanup);

	/**
	 * Compiles the AST to byte code and stores the generated .class files in the bin directory.
	 */
	ICompilerPhase COMPILE = new SequentialCompilerPhase(200, "COMPILE", ICompilationUnit::compile);

	/**
	 * Tests the main type specified in {@link CompilerConfig#mainType}.
	 */
	ICompilerPhase TEST = new CompilerPhase(1000, "TEST", DyvilCompiler::test);

	String getName();

	int getID();

	void apply(DyvilCompiler compiler);

	@Override
	default int compareTo(@NonNull ICompilerPhase o)
	{
		return Integer.compare(this.getID(), o.getID());
	}
}