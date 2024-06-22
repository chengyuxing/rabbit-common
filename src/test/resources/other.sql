--#for item of :list delimiter ' and '
--#if :item == 'B'
message = :_for.item
      --  #fi
        --#for num of :item.nums delimiter ' or ' open '(' close ')'
          --  #if :num <> blank
            age = :_for.num and id = :_for.num
            --#fi
        --#done
--#done