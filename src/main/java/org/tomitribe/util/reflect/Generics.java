/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tomitribe.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Generics {

    private Generics() {
        // no-op
    }

    public static Type getType(final Field field) {
        return getTypeParameters(field.getType(), field.getGenericType())[0];
    }

    public static Type getType(final Parameter parameter) {
        return getTypeParameters(parameter.getType(), parameter.getGenericType())[0];
    }

    public static Type getReturnType(final Method method) {
        return getTypeParameters(method.getReturnType(), method.getGenericReturnType())[0];
    }

    /**
     * Get the generic parameter for a specific interface we implement.  The generic types
     * of other interfaces the specified class may implement will be ignored and not reported.
     *
     * If the interface has multiple generic parameters then multiple types will be returned.
     * If the interface has no generic parameters, then a zero-length array is returned.
     * If the class does not implement this interface, null will be returned.
     *
     * @param intrface The interface that has generic parameters
     * @param clazz    The class implementing the interface and specifying the generic type
     * @return the parameter types for this interface or null if the class does not implement the interface
     */
    public static Type[] getInterfaceTypes(final Class<?> intrface, final Class<?> clazz) {
        if (!intrface.isAssignableFrom(clazz)) return null;

        { // Is this one of our immediate interfaces or super classes?
            final Optional<Type[]> types = genericTypes(clazz)
                    .filter(type -> type instanceof ParameterizedType)
                    .map(ParameterizedType.class::cast)
                    .filter(parameterizedType -> intrface.equals(parameterizedType.getRawType()))
                    .map(ParameterizedType::getActualTypeArguments)
                    .findFirst();

            if (types.isPresent()) return types.get();
        }

        // Look at our parent and interface parents for the type
        final Type[] types = declaredTypes(clazz)
                .filter(Objects::nonNull)
                .map(aClass -> getInterfaceTypes(intrface, aClass))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (types == null) {
            // Our parent does not implement this interface.  We are
            // assignable to this interface, so it must be coming from
            // a place we aren't yet looking.  Feature gap.
            return null;
        }

        // The types we got back may in fact have variables in them,
        // in which case we need resolve them.
        for (int i = 0; i < types.length; i++) {
            types[i] = resolveTypeVariable(types[i], clazz);
            types[i] = resolveParameterizedTypes(types[i], clazz);
        }

        return types;
    }

    private static Type resolveParameterizedTypes(final Type parameterized, final Class<?> clazz) {
        // If this isn't actually a variable, return what they passed us
        // as there is nothing to resolve
        if (!(parameterized instanceof ParameterizedType)) return parameterized;
        final ParameterizedType parameterizedType = (ParameterizedType) parameterized;

        final Type[] types = parameterizedType.getActualTypeArguments();
        boolean modified = false;
        // The types we got back may in fact have variables in them,
        // in which case we need resolve them.
        for (int i = 0; i < types.length; i++) {
            final Type original = types[i];
            types[i] = resolveTypeVariable(types[i], clazz);
            types[i] = resolveParameterizedTypes(types[i], clazz);
            if (!original.equals(types[i])) modified = true;
        }

        //  We didn't have any work to do
        if (!modified) return parameterized;

        return new ResolvedParameterizedType(parameterizedType, types);
    }

    private static class ResolvedParameterizedType implements ParameterizedType {
        private final ParameterizedType parameterizedType;
        private final Type[] actualTypesResolved;

        public ResolvedParameterizedType(final ParameterizedType parameterizedType, final Type[] actualTypes) {
            this.parameterizedType = parameterizedType;
            this.actualTypesResolved = actualTypes;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypesResolved;
        }

        @Override
        public Type getRawType() {
            return parameterizedType.getRawType();
        }

        @Override
        public Type getOwnerType() {
            return parameterizedType.getOwnerType();
        }

        @Override
        public String getTypeName() {
            return parameterizedType.getTypeName();
        }
    }

    private static Type resolveTypeVariable(final Type variable, final Class<?> clazz) {
        // If this isn't actually a variable, return what they passed us
        // as there is nothing to resolve
        if (!(variable instanceof TypeVariable)) return variable;
        final TypeVariable<?> typeVariable = (TypeVariable<?>) variable;

        // Where was this type variable declared?
        final GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

        // At the moment we only support type variables on class definitions
        // so if it isn't of type Class, return the unresolved variable
        if (!(genericDeclaration instanceof Class)) return variable;
        final Class<?> declaringClass = (Class<?>) genericDeclaration;

        // Get the position of the variable in the generic signature
        // where variable names are specified
        final int typePosition = positionOf(variable, declaringClass.getTypeParameters());

        // We cannot seem to find our type variable in the list of parameters?
        // This shouldn't happen, but it did.  Return the unresolved variable
        if (typePosition == -1) return variable;

        // Get the actual type arguments passed from the place where the declaringClass
        // was used by clazz in either an 'extends' or 'implements' context
        final Type[] actualTypes = genericTypes(clazz)
                .filter(type -> type instanceof ParameterizedType)
                .map(ParameterizedType.class::cast)
                .filter(parameterizedType -> declaringClass.equals(parameterizedType.getRawType()))
                .map(ParameterizedType::getActualTypeArguments)
                .findFirst().orElse(null);

        // We cannot seem to find where the types are specified. We have a
        // feature gap in our code. Return the unresolved variable
        if (actualTypes == null) return variable;

        // We found where the actual types were supplied, but somehow the
        // array lengths don't line up? This shouldn't happen, but did.
        // Return the unresolved variable
        if (actualTypes.length != declaringClass.getTypeParameters().length) return variable;

        final Type resolvedType = actualTypes[typePosition];

        return resolvedType;
    }

    private static Stream<Type> genericTypes(Class<?> clazz) {
        return Stream.concat(Stream.of(clazz.getGenericSuperclass()), Stream.of(clazz.getGenericInterfaces()));
    }

    private static Stream<Class<?>> declaredTypes(Class<?> clazz) {
        return Stream.concat(Stream.of(clazz.getSuperclass()), Stream.of(clazz.getInterfaces()));
    }

    private static int positionOf(final Type variable, final TypeVariable<? extends Class<?>>[] typeParameters) {
        for (int i = 0; i < typeParameters.length; i++) {
            final TypeVariable<? extends Class<?>> typeParameter = typeParameters[i];
            if (variable.equals(typeParameter)) return i;
        }
        return -1;
    }

    public static Type[] getTypeParameters(final Class genericClass, final Type type) {
        if (type instanceof Class) {
            final Class rawClass = (Class) type;

            // if this is the collection class we're done
            if (genericClass.equals(type)) return null;

            for (final Type intf : rawClass.getGenericInterfaces()) {
                final Type[] collectionType = getTypeParameters(genericClass, intf);

                if (collectionType != null) return collectionType;
            }

            final Type[] collectionType = getTypeParameters(genericClass, rawClass.getGenericSuperclass());
            return collectionType;

        } else if (type instanceof ParameterizedType) {

            final ParameterizedType parameterizedType = (ParameterizedType) type;

            final Type rawType = parameterizedType.getRawType();

            if (genericClass.equals(rawType)) {

                final Type[] argument = parameterizedType.getActualTypeArguments();
                return argument;

            }

            final Type[] collectionTypes = getTypeParameters(genericClass, rawType);

            if (collectionTypes != null) {

                for (int i = 0; i < collectionTypes.length; i++) {

                    if (collectionTypes[i] instanceof TypeVariable) {

                        final TypeVariable typeVariable = (TypeVariable) collectionTypes[i];
                        final TypeVariable[] rawTypeParams = ((Class) rawType).getTypeParameters();

                        for (int j = 0; j < rawTypeParams.length; j++) {

                            if (typeVariable.getName().equals(rawTypeParams[j].getName())) {
                                collectionTypes[i] = parameterizedType.getActualTypeArguments()[j];
                            }
                        }
                    }
                }
            }

            return collectionTypes;
        }

        return null;
    }

}
