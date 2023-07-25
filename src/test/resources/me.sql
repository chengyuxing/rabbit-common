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
-- #for pair of :data | pairs delimiter ',\n'
    ${pair.item1} = :_for.pair.item2
-- #done
where id = :id;

select * from test.user where
id = 1
-- #for id of :ids delimiter ', \n' open ' or id in (' close ')'
    -- #if :id >= 5
    :_for.id
    -- #fi
-- #done
;