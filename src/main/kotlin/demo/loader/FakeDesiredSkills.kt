package demo.loader

import demo.models.offers.DesiredSkill
import demo.models.offers.SkillLevel

object FakeDesiredSkills {
    val desiredSkills: List<DesiredSkill> = listOf(
        DesiredSkill(null, "Python", SkillLevel.ADVANCED),
        DesiredSkill(null, "AWS", SkillLevel.NICE_TO_HAVE),
        DesiredSkill(null, "Scrum", SkillLevel.JUNIOR),
        DesiredSkill(null, "Team work", SkillLevel.JUNIOR),
        DesiredSkill(null, "Haskell", SkillLevel.NICE_TO_HAVE),
        DesiredSkill(null, "Ocaml", SkillLevel.NICE_TO_HAVE),
        DesiredSkill(null, "Java", SkillLevel.MASTER),
        DesiredSkill(null, "Looting", SkillLevel.MASTER),
        DesiredSkill(null, "Throw pillum", SkillLevel.MASTER),
    )
}