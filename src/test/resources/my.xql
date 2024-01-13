select *
from test.user
where
 #if :id != blank
    id = :id
 #fi
 #if :name != blank
  and name = :name
 #fi
 #if :age > 10
  and age = :age
 #fi