package ic.ac.uk.db_pcr_backend.model;

public class AccountInfoModel {
    public String _account_id; // The numeric ID of the account.
    public String name; // The full name of the user. Only set if detailed account information is
                        // requested. See option DETAILED_ACCOUNTS for change queries and option DETAILS
                        // for account queries.
    public String display_name; // The display name of the user. Only set if detailed account information is
                                // requested. See option DETAILED_ACCOUNTS for change queries and option DETAILS
                                // for account queries.
    public String username; // The username of the user. Only set if detailed account information is
                            // requested. See option DETAILED_ACCOUNTS for change queries and option DETAILS
                            // for account queries.
}