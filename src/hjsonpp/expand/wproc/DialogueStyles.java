package hjsonpp.expand.wproc;

import arc.struct.ObjectMap;

public enum DialogueStyles{
    bordless,
    border;
    //nametag
    //unitless

    public static ObjectMap<DialogueStyles, String> stylesMap =  new ObjectMap<>();

    static{
        stylesMap.put(bordless,"bordless");
        stylesMap.put(border, "border");
        /*stylesMap.put(nametag, "nametag");
        stylesMap.put(unitless, "unitless");*/
    }

    public static final DialogueStyles[] all = values();
}
