package org.nature.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Menu extends BaseModel {

    private String name;

    private List<List<PageInfo>> list;

}
