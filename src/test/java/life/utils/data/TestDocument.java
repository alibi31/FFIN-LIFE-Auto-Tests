package life.utils.data;

public record TestDocument(
        String number,
        int type,
        String authority,
        String issueDateStr
) {}