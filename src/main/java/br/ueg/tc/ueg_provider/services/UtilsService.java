package br.ueg.tc.ueg_provider.services;

import br.ueg.tc.pipa_integrator.annotations.ServiceProviderClass;
import br.ueg.tc.pipa_integrator.annotations.ServiceProviderMethod;
import br.ueg.tc.ueg_provider.serviceprovider.InstitutionService;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class UtilsService {

    private static String prefixPackage = "br.ueg.tc.ueg_provider.serviceprovider";

    public static Set<String> listAllServiceProviderBeans() {
        Reflections reflections = new Reflections(prefixPackage);

        Set<Class<? extends InstitutionService>> implementations = reflections.getSubTypesOf(InstitutionService.class);
        return implementations.stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
    }

    public static Class<? extends InstitutionService> getServiceProviderByName(String serviceName) {
        Reflections reflections = new Reflections(prefixPackage);

        return reflections.getSubTypesOf(InstitutionService.class)
                .stream()
                .filter(t -> t.getName().contains(serviceName))
                .findFirst().orElse(null);
    }

    public static List<String> listAllProviderServicesByPersona(List<String> personas) {
        return listAllServiceProviderBeans().stream()
                .map(UtilsService::getServiceProviderByName)
                .filter(Objects::nonNull)
                .filter(clazz -> clazz.isAnnotationPresent(ServiceProviderClass.class))
                .filter(clazz -> {
                    ServiceProviderClass annotation = clazz.getAnnotation(ServiceProviderClass.class);
                    List<String> classPersonas = Arrays.asList(annotation.personas());
                    return classPersonas.stream().anyMatch(personas::contains);
                })
                .map(Class::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getActionNamesByPersona(String persona) {
        Reflections reflections = new Reflections(prefixPackage);

        return reflections.getSubTypesOf(InstitutionService.class).stream()
                .filter(clazz -> clazz.isAnnotationPresent(ServiceProviderClass.class))
                .filter(clazz -> {
                    ServiceProviderClass annotation = clazz.getAnnotation(ServiceProviderClass.class);
                    return Arrays.asList(annotation.personas()).contains(persona);
                })
                .flatMap(clazz -> {
                    Method[] methods = clazz.getDeclaredMethods();
                    return Arrays.stream(methods)
                            .filter(method -> method.isAnnotationPresent(ServiceProviderMethod.class))
                            .map(method -> method.getAnnotation(ServiceProviderMethod.class).actionName());
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
