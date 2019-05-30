package com.github.kr328.webapi.store;

import com.alibaba.fastjson.JSONObject;
import com.github.kr328.webapi.model.Metadata;
import com.github.kr328.webapi.utils.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Comparator;

public class StoreManager {
    private String dataPath;

    public StoreManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public Metadata save(String username, long userId, long messageId, File dataFile) throws IOException {
        Metadata metadata = new Metadata(username, userId, messageId, RandomUtils.randomSecret());
        String userIdString = String.valueOf(userId);

        Files.createDirectories(Paths.get(dataPath,userIdString));
        Files.copy(dataFile.toPath(), Paths.get(dataPath, userIdString, "data.yml"), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(dataFile.toPath());
        Files.writeString(Paths.get(dataPath, userIdString, "metadata.json"), JSONObject.toJSONString(metadata), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

        return metadata;
    }

    public void delete(long userId) throws IOException {
        Files.walk(Paths.get(dataPath, String.valueOf(userId)))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
