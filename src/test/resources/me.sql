select * from user where id = 1
--#if :c <> blank
    --#if :c1 <> blank
        and t.c1 = :c1
    --#fi
    --#if :c2 <> blank
        and t.c2 = :c2
   -- #fi
--#fi

-- #for item of :list delimiter ' and\n '
        -- #if :item == 'B'
            message = :_for.item
        --#fi
        -- bbbbbb
        -- #for num,idx of :item.nums delimiter ' or\n ' open '(' close ')'
            --#if :num <> blank
            age = :_for.num and id = :_for.idx
            --#fi
        -- #done
-- #done
;

update test.user
set
-- #for item of :data | kv delimiter ', '
    ${item.key} = :_for.item.value
-- #done
where id = :id;

select * from test.user where
id = 1
-- #for id of :ids delimiter ', \n' open " or i''d in (" close ')'
    -- #if :id >= 5
    :_for.id
    -- #fi
-- #done
;

select * from test.user where
-- #if :id != blank
    id = :id
-- #or
    id = 10
-- #fi
;

select * from test.user where
-- #if :id != blank
    id = :id
-- #or :id > 10
    id = 10
-- #or
    id = 1
-- #fi
;