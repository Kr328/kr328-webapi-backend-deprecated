package com.github.kr328.webapi.api;

import com.github.kr328.webapi.api.clash.model.ClashPreprocessorRoot;
import com.github.kr328.webapi.api.clash.model.ClashRoot;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ClashPreprocessor {
    public static Mono<String> process(String data) {
        ClashPreprocessorRoot root = load(data);



        return Mono.empty();
    }

    public static void main(String[] args) throws Exception {
        ClashRoot root = loadClash(Files.readString(Paths.get("/home/null/Downloads/config.yml")));

        Yaml yaml = new Yaml();

        System.out.println(yaml.dumpAsMap(root));

        //System.out.println(root);
    }

    private static ClashRoot loadClash(String data) {
        Constructor constructor = new Constructor(ClashRoot.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {setSkipMissingProperties(true);}
            @Override
            public Property getProperty(Class<?> type, String name) {
                StringBuilder result = new StringBuilder();
                for ( String s : name.split("-+") ) {
                    char[] cs = s.toCharArray();
                    if ( cs.length > 0 ) cs[0] = Character.toUpperCase(cs[0]);
                    result.append(cs);
                }
                if ( result.length() > 0 ) result.setCharAt(0 ,Character.toLowerCase(result.charAt(0)));
                return super.getProperty(type, result.toString());
            }
        });

        Yaml yaml = new Yaml(constructor);

        return yaml.loadAs(data, ClashRoot.class);
    }

    private static ClashPreprocessorRoot load(String data) {
        Constructor constructor = new Constructor(ClashPreprocessorRoot.class);

        constructor.setPropertyUtils(new PropertyUtils() {
            {setSkipMissingProperties(true);}
            @Override
            public Property getProperty(Class<?> type, String name) {
                StringBuilder result = new StringBuilder();
                for ( String s : name.split("-+") ) {
                    char[] cs = s.toCharArray();
                    if ( cs.length > 0 ) cs[0] = Character.toUpperCase(cs[0]);
                    result.append(cs);
                }
                if ( result.length() > 0 ) result.setCharAt(0 ,Character.toLowerCase(result.charAt(0)));
                return super.getProperty(type, result.toString());
            }
        });

        Yaml yaml = new Yaml(constructor);

        return yaml.loadAs(data, ClashPreprocessorRoot.class);
    }
}
