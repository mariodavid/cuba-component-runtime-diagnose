import com.haulmont.cuba.security.entity.Group
import com.haulmont.cuba.core.global.LoadContext


def user = metadata.create('sec$User')
user.login = "mario"
user.password = "mario"
def uuid = UUID.fromString("0fa2b1a5-1d68-4d69-9fbd-dff348347f93")
LoadContext loadContext = LoadContext.create(com.haulmont.cuba.security.entity.Group).setId(uuid).setView("_minimal")
user.group = dataManager.load(loadContext)

dataManager.commit(user)