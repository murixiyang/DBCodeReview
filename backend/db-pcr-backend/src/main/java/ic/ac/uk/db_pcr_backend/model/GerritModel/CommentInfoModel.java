package ic.ac.uk.db_pcr_backend.model.GerritModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentInfoModel {
    public String id; // The URL encoded UUID of the comment.
    public String path; // The file path for which the inline comment was done. Not set if returned in a
                        // map where the key is the file path.
    public String side; // The side on which the comment was added. Allowed values are REVISION and
                        // PARENT. If not set, the default is REVISION.
    public Integer line; // The number of the line for which the comment was done. If range is set, this
                         // equals the end line of the range. If neither line nor range is set, it’s a
                         // file comment.
    public CommentRangeModel range; // The range of the comment as a CommentRange entity.
    public String in_reply_to; // The URL encoded UUID of the comment to which this comment is a reply.
    public String message; // The comment message.
    public String updated; // The timestamp of when this comment was written.
    public AccountInfoModel author; // The author of the message as an AccountInfo entity. Unset for draft comments,
                                    // assumed to be the calling user.
}