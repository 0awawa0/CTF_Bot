package new_db

import org.jetbrains.exposed.dao.id.EntityID

abstract class BaseDTO {
    abstract val id: EntityID<Long>
}

data class CompetitionDTO(val entity: CompetitionEntity): BaseDTO() {
    override val id: EntityID<Long> = entity.id


}