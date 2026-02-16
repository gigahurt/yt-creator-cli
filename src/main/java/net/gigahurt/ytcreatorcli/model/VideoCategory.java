package net.gigahurt.ytcreatorcli.model;

public enum VideoCategory {

    FILM_AND_ANIMATION("1", "Film & Animation"),
    AUTOS_AND_VEHICLES("2", "Autos & Vehicles"),
    MUSIC("10", "Music"),
    PETS_AND_ANIMALS("15", "Pets & Animals"),
    SPORTS("17", "Sports"),
    TRAVEL_AND_EVENTS("19", "Travel & Events"),
    GAMING("20", "Gaming"),
    PEOPLE_AND_BLOGS("22", "People & Blogs"),
    COMEDY("23", "Comedy"),
    ENTERTAINMENT("24", "Entertainment"),
    NEWS_AND_POLITICS("25", "News & Politics"),
    HOWTO_AND_STYLE("26", "Howto & Style"),
    EDUCATION("27", "Education"),
    SCIENCE_AND_TECHNOLOGY("28", "Science & Technology"),
    NONPROFITS_AND_ACTIVISM("29", "Nonprofits & Activism");

    private final String id;
    private final String displayName;

    VideoCategory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VideoCategory fromId(String id) {
        for (VideoCategory cat : values()) {
            if (cat.id.equals(id)) {
                return cat;
            }
        }
        return null;
    }

    public static VideoCategory fromName(String name) {
        try {
            return valueOf(name.toUpperCase().replace(' ', '_').replace('&', 'A'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String listAll() {
        StringBuilder sb = new StringBuilder();
        for (VideoCategory cat : values()) {
            sb.append(String.format("  %s = %s%n", cat.id, cat.displayName));
        }
        return sb.toString();
    }
}
