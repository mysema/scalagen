/*
 * Copyright (C) 2011, Mysema Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.mysema.scalagen

import japa.parser.ast.visitor._
import java.util.ArrayList
import japa.parser.ast.ImportDeclaration
import japa.parser.ast.expr.NameExpr
import japa.parser.ast.visitor.ModifierVisitorAdapter
import UnitTransformer._

/**
 * Annotations turns Annotation type declarations into normal classes which extend
 * StaticAnnotation
 */
class Annotations(targetVersion: ScalaVersion) extends UnitTransformerBase {
  
  private val staticAnnotationType = new ClassOrInterface("StaticAnnotation")
  
  def transform(cu: CompilationUnit): CompilationUnit = {
    cu.accept(this, cu).asInstanceOf[CompilationUnit] 
  }  
    
  override def visit(n: AnnotationDecl, arg: CompilationUnit) = {
    // turns annotations into StaticAnnotation subclasses
    if (targetVersion == Scala210) {
      //StaticAnnotation was in the "scala" package in 2.9, so it was imported by default
      //in scala 2.10, it was moved to the scala.annotation package, so we need an explicit import
      arg.getImports().add(new ImportDeclaration(new NameExpr("scala.annotation.StaticAnnotation"), false, false))
    }
    val clazz = new ClassOrInterfaceDecl()
    clazz.setName(n.getName)    
    clazz.setExtends(staticAnnotationType :: Nil)
    clazz.setMembers(createMembers(n))
    clazz
  }
  
  private def createMembers(n: AnnotationDecl): JavaList[BodyDecl] = {
    // TODO : default values
    val params = n.getMembers.collect { case m: AnnotationMember => m }
      .map(m => new Parameter(PROPERTY, m.getType, new VariableDeclaratorId(m.getName)))
      
    if (!params.isEmpty) {
      val constructor = new Constructor()
      constructor.setParameters(params)
      constructor.setBlock(new Block())
      constructor :: Nil
    } else {
      Nil
    }
  }
    
}  