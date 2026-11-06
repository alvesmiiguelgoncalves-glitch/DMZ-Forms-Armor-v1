package com.dmzssj4armors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Le {@code config/dmz_ssj4_armors-forms.json}: quais formas usam a roupa alternativa.
 *
 * <p>As chaves seguem o mesmo padrao das chaves de lang do DMZ:
 * {@code race.dragonminez.<raca>.form.<grupo>.<forma>}. O valor e {@code true} (roupa padrao do
 * addon), {@code false} (nao troca) ou o nome de uma pasta de texturas, como {@code "ssj3"}
 * (usa {@code textures/armor/ssj3/...} do addon ou de qualquer resource pack). O arquivo e
 * relido quando muda, sem reiniciar o jogo.</p>
 */
public final class FormsConfig {

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("dmz_ssj4_armors-forms.json");

    private static final String DEFAULT_TEXT = """
            {
              "race.dragonminez.saiyan.form.oozaru.supersaiyan4": true,
              "race.dragonminez.saiyan.form.oozaru.superfullpowersaiyan4": true,
              "race.dragonminez.saiyan.form.supersaiyan.supersaiyan4": true,
              "race.dragonminez.saiyan.form.legendaryforms.ssjfullpower": true
            }
            """;

    /** Resultado de uma entrada: {@code profile} vazio = roupa padrao do addon; senao, nome da pasta. */
    public record Entry(boolean enabled, String profile) {
    }

    private static final Entry OFF = new Entry(false, "");
    private static final Entry DEFAULT = new Entry(true, "");

    private static long loadedStamp = Long.MIN_VALUE;
    private static Map<String, Entry> cached = new HashMap<>();

    private FormsConfig() {
    }

    /** @return a entrada da forma, ou {@code null} se ela nao esta no arquivo. */
    @Nullable
    public static synchronized Entry lookup(String race, String group, String form) {
        reloadIfChanged();
        String key = ("race.dragonminez." + race + ".form." + group + "." + form).toLowerCase(Locale.ROOT);
        return cached.get(key);
    }

    private static void reloadIfChanged() {
        try {
            if (!Files.exists(FILE)) {
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, DEFAULT_TEXT, StandardCharsets.UTF_8);
            }
            long stamp = Files.getLastModifiedTime(FILE).toMillis();
            if (stamp == loadedStamp) return;
            loadedStamp = stamp;

            Map<String, Entry> out = new HashMap<>();
            JsonElement root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8));
            if (root.isJsonObject()) {
                JsonObject object = root.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    JsonElement value = entry.getValue();
                    String key = entry.getKey().trim().toLowerCase(Locale.ROOT);
                    if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
                        out.put(key, value.getAsBoolean() ? DEFAULT : OFF);
                    } else if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        String profile = value.getAsString().trim().toLowerCase(Locale.ROOT);
                        if (profile.matches("[a-z0-9_.\\-]+(/[a-z0-9_.\\-]+)*")) {
                            out.put(key, new Entry(true, profile));
                        } else {
                            DmzSsj4Armors.LOGGER.warn("{}: o nome de pasta \"{}\" da forma {} so pode ter letras minusculas, numeros, _ - . e /; ignorado.",
                                    FILE.getFileName(), value.getAsString(), key);
                        }
                    }
                }
            } else {
                DmzSsj4Armors.LOGGER.warn("{} precisa ser um objeto JSON; ignorando.", FILE.getFileName());
            }
            cached = out;
        } catch (IOException | RuntimeException e) {
            // JSON invalido ou arquivo ilegivel: mantem o que ja estava carregado e avisa uma vez.
            if (loadedStamp != Long.MAX_VALUE) {
                DmzSsj4Armors.LOGGER.warn("Erro ao ler {} ({}); mantendo a configuracao anterior.", FILE.getFileName(), e.toString());
            }
        }
    }
}
