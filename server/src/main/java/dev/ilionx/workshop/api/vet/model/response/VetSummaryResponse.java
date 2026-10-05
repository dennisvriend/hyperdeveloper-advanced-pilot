package dev.ilionx.workshop.api.vet.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Summary response DTO for a veterinarian embedded within visit responses.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Summary of the veterinarian who performed a visit")
public class VetSummaryResponse {

    @Schema(
        description = "The unique identifier of the veterinarian",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer id;

    @Schema(
        description = "The veterinarian's first name",
        example = "James",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;

    @Schema(
        description = "The veterinarian's last name",
        example = "Carter",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;
}
