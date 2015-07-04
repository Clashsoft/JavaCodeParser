package dyvil.tools.compiler.phase;

import dyvil.lang.Collection;

import dyvil.io.FileUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.lexer.token.IdentifierToken;

public interface ICompilerPhase extends Comparable<ICompilerPhase>
{
	/**
	 * Recursively deletes all files in the output directory.
	 * 
	 * @see DyvilCompiler#clean()
	 */
	ICompilerPhase	CLEAN			= new CompilerPhase(0, "CLEAN", units -> DyvilCompiler.clean());
	
	/**
	 * Splits the input file into {@link IdentifierToken Tokens} using
	 * {@link Dlex}.
	 */
	ICompilerPhase	TOKENIZE		= new ParallelCompilerPhase(10, "TOKENIZE", ICompilationUnit::tokenize);
	
	/**
	 * Parses the list of tokens generated by {@link #TOKENIZE}.
	 */
	ICompilerPhase	PARSE			= new ParallelCompilerPhase(20, "PARSE", ICompilationUnit::parse);
	
	/**
	 * Prints the AST.
	 */
	ICompilerPhase	PRINT			= new PrintPhase(30);
	
	/**
	 * Saves the formatted AST to the input file
	 */
	ICompilerPhase	FORMAT			= new ParallelCompilerPhase(40, "FORMAT", unit -> FileUtils.write(unit.getInputFile(), unit.toString()));
	
	/**
	 * Resolves packages, classes and types.
	 */
	ICompilerPhase	RESOLVE_TYPES	= new ParallelCompilerPhase(50, "RESOLVE_TYPES", ICompilationUnit::resolveTypes);
	
	/**
	 * Resolves methods and field names.
	 */
	ICompilerPhase	RESOLVE			= new ParallelCompilerPhase(60, "RESOLVE", ICompilationUnit::resolve);
	
	/**
	 * Resolves other things such as lambda expressions or annotations and
	 * checks types. This will be called after
	 * {@link IValue#withType(IType, ITypeContext, MarkerList, IContext)} has
	 * been called. Mainly used by
	 * {@link IMethod#checkArguments(MarkerList, ICodePosition, IContext, IValue, IArguments, ITypeContext)}
	 * .
	 */
	ICompilerPhase	CHECK_TYPES		= new ParallelCompilerPhase(70, "CHECK_TYPES", ICompilationUnit::checkTypes);
	
	/**
	 * Checks for semantical errors. The general contract of this method is that
	 * it should not be mandatory for a compilation to succeed (given that
	 * everything is correct). Thus, it should not do any more linking or
	 * resolving.
	 */
	ICompilerPhase	CHECK			= new ParallelCompilerPhase(80, "CHECK", ICompilationUnit::check);
	
	/**
	 * Folds constants.
	 */
	ICompilerPhase	FOLD_CONSTANTS	= new FoldConstantPhase(90);
	
	/**
	 * Performs the final cleanup on the AST. This also includes special
	 * registrations such as adding Lambda Expressions to the list of compilable
	 * elements in classes.
	 */
	ICompilerPhase	CLEANUP			= new ParallelCompilerPhase(100, "CLEANUP", ICompilationUnit::cleanup);
	
	/**
	 * Compiles the AST to byte code and stores the generated .class files in
	 * the bin directory.
	 */
	ICompilerPhase	COMPILE			= new ParallelCompilerPhase(200, "COMPILE", ICompilationUnit::compile);
	
	/**
	 * Converts the .class files in the bin directory to a JAR file, sets up the
	 * classpath and signs the JAR.
	 */
	ICompilerPhase	JAR				= new CompilerPhase(210, "JAR", units -> ClassWriter.generateJAR(DyvilCompiler.fileFinder.files));
	
	/**
	 * Tests the main type specified in {@link CompilerConfig#mainType}.
	 */
	ICompilerPhase	TEST			= new CompilerPhase(1000, "TEST", units -> DyvilCompiler.test());
	
	public String getName();
	
	public int getID();
	
	public void apply(Collection<ICompilationUnit> units);
	
	@Override
	public default int compareTo(ICompilerPhase o)
	{
		return Integer.compare(this.getID(), o.getID());
	}
}
