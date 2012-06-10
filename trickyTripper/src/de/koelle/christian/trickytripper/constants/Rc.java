package de.koelle.christian.trickytripper.constants;

import java.text.Collator;

public class Rc {

    /**
     * Log-tags.
     */
    public static final String LT = "TT";
    public static final String LT_INPUT = "TT_INPUT";
    public static final String LT_DB = "TT_DB";
    public static final String LT_IO = "TT_IO";
    public static final String LT_PROV = "TT_PROV";

    public static final String TAB_SPEC_ID_PAYMENT = "payment";
    public static final String TAB_SPEC_ID_PARTICIPANTS = "participants";
    public static final String TAB_SPEC_ID_REPORT = "report";

    public static final String ACTIVITY_PARAM_KEY_PARTICIPANT = "participant";
    public static final String ACTIVITY_PARAM_KEY_PARTICIPANT_ID = "activityParamParticipantId";
    public static final String ACTIVITY_PARAM_KEY_PAYMENT_ID = "activityParamPaymentId";
    public static final String ACTIVITY_PARAM_KEY_VIEW_MODE = "viewMode";

    public static final String ACTIVITY_PARAM_VIEW_MODE_EDIT_MODE = "edit";
    public static final String ACTIVITY_PARAM_VIEW_MODE_CREATE_MODE = "create";

    public static final int DIALOG_SHOW_HELP = 100;

    public static final int DEFAULT_COLLATOR_STRENGTH = Collator.TERTIARY;

    public static boolean USE_CACHE_DIR_NOT_FILE_DIR_FOR_REPORTS = true;

    public static final String LINE_FEED = "\n";

}