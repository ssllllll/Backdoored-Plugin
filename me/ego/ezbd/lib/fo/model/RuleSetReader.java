package me.ego.ezbd.lib.fo.model;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import me.ego.ezbd.lib.fo.Common;
import me.ego.ezbd.lib.fo.FileUtil;
import me.ego.ezbd.lib.fo.Valid;

public abstract class RuleSetReader<T extends Rule> {
    private final String newKeyword;

    public RuleSetReader(String newKeyword) {
        this.newKeyword = newKeyword;
    }

    public abstract void load();

    public final void toggleMessage(Rule rule, boolean disabled) {
        File file = rule.getFile();
        Valid.checkBoolean(file.exists(), "No such file: " + file + " Rule: " + rule, new Object[0]);
        List<String> lines = FileUtil.readLines(file);
        boolean found = false;

        for(int i = 0; i < lines.size(); ++i) {
            String line = (String)lines.get(i);
            if (line.equals(this.newKeyword + " " + rule.getUid())) {
                found = true;
            } else if (!line.startsWith("#") && !line.isEmpty() && !line.startsWith("match ")) {
                if (line.equals("disabled") && found && !disabled) {
                    lines.remove(i);
                    break;
                }
            } else if (found && i > 0 && disabled) {
                lines.add(i, "disabled");
                break;
            }
        }

        Valid.checkBoolean(found, "Failed to disable rule " + rule, new Object[0]);
        this.saveAndLoad(file, lines);
    }

    protected final void saveAndLoad(File file, List<String> lines) {
        FileUtil.write(file, lines, new StandardOpenOption[]{StandardOpenOption.TRUNCATE_EXISTING});
        this.load();
    }

    protected final List<T> loadFromFile(String path) {
        File file = FileUtil.extract(path);
        return this.loadFromFile(file);
    }

    private final List<T> loadFromFile(File file) {
        List<T> rules = new ArrayList();
        List<String> lines = FileUtil.readLines(file);
        T rule = null;
        String match = null;

        for(int i = 0; i < lines.size(); ++i) {
            String line = ((String)lines.get(i)).trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                if (line.startsWith(this.newKeyword + " ")) {
                    if (rule != null && this.canFinish(rule)) {
                        rules.add(rule);
                    }

                    try {
                        match = line.replace(this.newKeyword + " ", "");
                        rule = this.createRule(file, match);
                    } catch (Throwable var10) {
                        Common.throwError(var10, new String[]{"Error creating rule from line (" + (i + 1) + "): " + line, "File: " + file, "Error: %error", "Processing aborted."});
                        return rules;
                    }
                } else {
                    if (!this.onNoMatchLineParse(file, line)) {
                        Valid.checkNotNull(match, "Cannot define operator when no rule is being created! File: '" + file + "' Line (" + (i + 1) + "): '" + line + "'");
                    }

                    if (rule != null) {
                        try {
                            rule.onOperatorParse(line.split(" "));
                        } catch (Throwable var9) {
                            Common.throwError(var9, new String[]{"Error parsing rule operator from line (" + (i + 1) + "): " + line, "File: " + file, "Error: %error"});
                        }
                    }
                }
            }

            if (i + 1 == lines.size() && rule != null && this.canFinish(rule)) {
                rules.add(rule);
            }
        }

        return rules;
    }

    protected boolean onNoMatchLineParse(File file, String line) {
        return false;
    }

    protected boolean canFinish(T rule) {
        return true;
    }

    @Nullable
    protected abstract T createRule(File var1, String var2);
}