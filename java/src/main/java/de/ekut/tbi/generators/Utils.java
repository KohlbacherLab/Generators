package de.ekut.tbi.generators;



import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.util.Optional;
import java.util.Collection;
import java.util.Map;


final class Utils 
{

 
  static boolean isParameterized(Type t)
  {
    try {
      ParameterizedType.class.cast(t);
      return true;
    } catch (ClassCastException cce){
      return false;
    }
  }


  static boolean isOptional(Type type)
  {
    try {
      return
        Optional.ofNullable(ParameterizedType.class.cast(type))
          .map(t -> t.getRawType())
          .map(Class.class::cast)
          .filter(c -> c.equals(Optional.class))
          .isPresent();
    } catch (ClassCastException cce){
      return false;
    }
  }


  static boolean isCollection(Type type)
  {
    try {
      return
        Optional.ofNullable(ParameterizedType.class.cast(type))
          .map(t -> t.getRawType())
          .map(Class.class::cast)
          .filter(Collection.class::isAssignableFrom)
          .isPresent();
    } catch (ClassCastException cce){
      return false;
    }
  }


  static boolean isMap(Type type)
  {
    try {
      return
        Optional.ofNullable(ParameterizedType.class.cast(type))
          .map(t -> t.getRawType())
          .map(Class.class::cast)
          .filter(Map.class::isAssignableFrom)
          .isPresent();
    } catch (ClassCastException cce){
      return false;
    }
  }


  static Type getParameterType(Type type)
  {
    return getParameterType(type,0);
  }

  static Type getParameterType(Type type, int i)
  {
    return ParameterizedType.class.cast(type).getActualTypeArguments()[i];
  }


  static Type[] getParameterTypes(Type type)
  {
    return ParameterizedType.class.cast(type).getActualTypeArguments();
  }


  static Class<?> getRawType(Type type)
  {
    return Optional.ofNullable(ParameterizedType.class.cast(type))
                   .map(t -> t.getRawType())
                   .map(Class.class::cast)
                   .get();
  }






}

