package me.ego.ezbd.lib.fo.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import me.ego.ezbd.lib.fo.collection.SerializedMap;
import me.ego.ezbd.lib.fo.command.annotation.Permission;
import me.ego.ezbd.lib.fo.command.annotation.PermissionGroup;
import me.ego.ezbd.lib.fo.constants.FoPermissions;
import me.ego.ezbd.lib.fo.exception.FoException;
import me.ego.ezbd.lib.fo.model.ChatPaginator;
import me.ego.ezbd.lib.fo.model.Replacer;
import me.ego.ezbd.lib.fo.model.SimpleComponent;
import me.ego.ezbd.lib.fo.settings.SimpleLocalization.Commands;

public final class PermsCommand extends SimpleSubCommand {
    private final Class<? extends FoPermissions> classToList;
    private final SerializedMap variables;

    public PermsCommand(@NonNull Class<? extends FoPermissions> classToList) {
        this(classToList, new SerializedMap());
        if (classToList == null) {
            throw new NullPointerException("classToList is marked non-null but is null");
        }
    }

    public PermsCommand(@NonNull Class<? extends FoPermissions> classToList, SerializedMap variables) {
        super("permissions|perms");
        if (classToList == null) {
            throw new NullPointerException("classToList is marked non-null but is null");
        } else {
            this.classToList = classToList;
            this.variables = variables;
            this.setDescription(Commands.PERMS_DESCRIPTION);
            this.setUsage(Commands.PERMS_USAGE);
            this.list();
        }
    }

    protected void onCommand() {
        String phrase = this.args.length > 0 ? this.joinArgs(0) : null;
        (new ChatPaginator(15)).setFoundationHeader(Commands.PERMS_HEADER).setPages(this.list(phrase)).send(this.sender);
    }

    private List<SimpleComponent> list() {
        return this.list((String)null);
    }

    private List<SimpleComponent> list(String phrase) {
        List<SimpleComponent> messages = new ArrayList();
        Class iteratedClass = this.classToList;

        try {
            do {
                this.listIn(iteratedClass, messages, phrase);
            } while(!(iteratedClass = iteratedClass.getSuperclass()).isAssignableFrom(Object.class));
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return messages;
    }

    private void listIn(Class<?> clazz, List<SimpleComponent> messages, String phrase) throws ReflectiveOperationException {
        if (!clazz.isAssignableFrom(FoPermissions.class)) {
            PermissionGroup group = (PermissionGroup)clazz.getAnnotation(PermissionGroup.class);
            if (!messages.isEmpty() && !clazz.isAnnotationPresent(PermissionGroup.class)) {
                throw new FoException("Please place @PermissionGroup over " + clazz);
            }

            messages.add(SimpleComponent.of("&7- " + (messages.isEmpty() ? Commands.PERMS_MAIN : group.value()) + " " + Commands.PERMS_PERMISSIONS).onClickOpenUrl(""));
        }

        Field[] var13 = clazz.getDeclaredFields();
        int var5 = var13.length;

        int var6;
        for(var6 = 0; var6 < var5; ++var6) {
            Field field = var13[var6];
            if (field.isAnnotationPresent(Permission.class)) {
                Permission annotation = (Permission)field.getAnnotation(Permission.class);
                String info = Replacer.replaceVariables(annotation.value(), this.variables);
                boolean def = annotation.def();
                if (info.contains("{") && info.contains("}")) {
                    throw new FoException("Forgotten unreplaced variable in " + info + " for field " + field + " in " + clazz);
                }

                String node = Replacer.replaceVariables((String)field.get((Object)null), this.variables);
                boolean has = this.sender == null ? false : this.hasPerm(node);
                if (phrase == null || node.contains(phrase)) {
                    messages.add(SimpleComponent.of("  " + (has ? "&a" : "&7") + node + (def ? " " + Commands.PERMS_TRUE_BY_DEFAULT : "")).onClickOpenUrl("").onHover(new String[]{Commands.PERMS_INFO + info, Commands.PERMS_DEFAULT + (def ? Commands.PERMS_YES : Commands.PERMS_NO), Commands.PERMS_APPLIED + (has ? Commands.PERMS_YES : Commands.PERMS_NO)}));
                }
            }
        }

        Class[] var14 = clazz.getDeclaredClasses();
        var5 = var14.length;

        for(var6 = 0; var6 < var5; ++var6) {
            Class<?> inner = var14[var6];
            messages.add(SimpleComponent.of("&r "));
            this.listIn(inner, messages, phrase);
        }

    }

    protected List<String> tabComplete() {
        return NO_COMPLETE;
    }
}
