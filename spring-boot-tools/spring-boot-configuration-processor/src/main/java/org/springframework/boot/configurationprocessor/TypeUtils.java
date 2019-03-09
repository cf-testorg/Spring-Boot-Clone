/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.configurationprocessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

/**
 * Type Utilities.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.2.0
 */
class TypeUtils {

	private static final Map<TypeKind, Class<?>> PRIMITIVE_WRAPPERS;
	private static final Map<String, TypeKind> WRAPPER_TO_PRIMITIVE;

	static {
		Map<TypeKind, Class<?>> wrappers = new HashMap<TypeKind, Class<?>>();
		wrappers.put(TypeKind.BOOLEAN, Boolean.class);
		wrappers.put(TypeKind.BYTE, Byte.class);
		wrappers.put(TypeKind.CHAR, Character.class);
		wrappers.put(TypeKind.DOUBLE, Double.class);
		wrappers.put(TypeKind.FLOAT, Float.class);
		wrappers.put(TypeKind.INT, Integer.class);
		wrappers.put(TypeKind.LONG, Long.class);
		wrappers.put(TypeKind.SHORT, Short.class);
		PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(wrappers);

		Map<String, TypeKind> primitives = new HashMap<String, TypeKind>();
		primitives.put(Boolean.class.getName(), TypeKind.BOOLEAN);
		primitives.put(Byte.class.getName(), TypeKind.BYTE);
		primitives.put(Character.class.getName(), TypeKind.CHAR);
		primitives.put(Double.class.getName(), TypeKind.DOUBLE);
		primitives.put(Float.class.getName(), TypeKind.FLOAT);
		primitives.put(Integer.class.getName(), TypeKind.INT);
		primitives.put(Long.class.getName(), TypeKind.LONG);
		primitives.put(Short.class.getName(), TypeKind.SHORT);
		WRAPPER_TO_PRIMITIVE = primitives;
	}

	private final ProcessingEnvironment env;

	private final TypeMirror collectionType;

	private final TypeMirror mapType;

	TypeUtils(ProcessingEnvironment env) {
		this.env = env;
		Types types = env.getTypeUtils();
		WildcardType wc = types.getWildcardType(null, null);
		this.collectionType = types.getDeclaredType(
				this.env.getElementUtils().getTypeElement(Collection.class.getName()),
				wc);
		this.mapType = types.getDeclaredType(
				this.env.getElementUtils().getTypeElement(Map.class.getName()), wc, wc);
	}

	public String getType(Element element) {
		return getType(element == null ? null : element.asType());
	}

	public String getType(TypeMirror type) {
		if (type == null) {
			return null;
		}
		Class<?> wrapper = getWrapperFor(type);
		if (wrapper != null) {
			return wrapper.getName();
		}
		if (type instanceof DeclaredType) {
			DeclaredType declaredType = (DeclaredType) type;
			Element enclosingElement = declaredType.asElement().getEnclosingElement();
			if (enclosingElement != null && enclosingElement instanceof TypeElement) {
				return getType(enclosingElement) + "$"
						+ declaredType.asElement().getSimpleName().toString();
			}
		}
		return type.toString();
	}

	public boolean isCollectionOrMap(TypeMirror type) {
		return this.env.getTypeUtils().isAssignable(type, this.collectionType)
				|| this.env.getTypeUtils().isAssignable(type, this.mapType);
	}


	public boolean isEnclosedIn(Element candidate, TypeElement element) {
		if (candidate == null || element == null) {
			return false;
		}
		if (candidate.equals(element)) {
			return true;
		}
		return isEnclosedIn(candidate.getEnclosingElement(), element);
	}

	public String getJavaDoc(Element element) {
		String javadoc = (element == null ? null
				: this.env.getElementUtils().getDocComment(element));
		if (javadoc != null) {
			javadoc = javadoc.trim();
		}
		return ("".equals(javadoc) ? null : javadoc);
	}

	public TypeMirror getWrapperOrPrimitiveFor(TypeMirror typeMirror) {
		Class<?> candidate = getWrapperFor(typeMirror);
		if (candidate != null) {
			return this.env.getElementUtils().getTypeElement(candidate.getName()).asType();
		}
		TypeKind primitiveKind = getPrimitiveFor(typeMirror);
		if (primitiveKind != null) {
			return this.env.getTypeUtils().getPrimitiveType(primitiveKind);
		}
		return null;
	}

	private Class<?> getWrapperFor(TypeMirror type) {
		return PRIMITIVE_WRAPPERS.get(type.getKind());
	}

	private TypeKind getPrimitiveFor(TypeMirror type) {
		return WRAPPER_TO_PRIMITIVE.get(type.toString());
	}

}
