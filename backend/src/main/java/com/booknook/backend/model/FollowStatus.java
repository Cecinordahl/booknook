package com.booknook.backend.model;

/**
 * ACTIVE follows show up in the normal series sections and get release-check notifications.
 * DISCARDED means the user explicitly unfollowed — kept (not deleted) so it can be shown in a
 * "Discarded" section and re-followed, and so a later book from the same series doesn't silently
 * re-follow something the user opted out of.
 */
public enum FollowStatus {
    ACTIVE,
    DISCARDED
}
