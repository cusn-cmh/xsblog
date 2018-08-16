package com.xqx.xsblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xqx.xsblog.entity.Menu;
import com.xqx.xsblog.entity.Role;
import com.xqx.xsblog.entity.User;
import com.xqx.xsblog.service.MenuService;
import com.xqx.xsblog.service.RoleService;
import com.xqx.xsblog.service.UserService;
import com.xqx.common.annotation.SysLog;
import com.xqx.common.base.PageData;
import com.xqx.common.util.ResponseEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("admin/system/role")
public class RoleController {

    @Autowired
    RoleService roleService;

    @Autowired
    UserService userService;

    @Autowired
    MenuService menuService;

    @GetMapping(value = "list")
    public String list(){
        return "admin/role/list";
    }

    @RequiresPermissions("sys:role:list")
    @PostMapping("list")
    @ResponseBody
    public PageData<Role> list(@RequestParam(value = "page",defaultValue = "1")Integer page,
                                @RequestParam(value = "limit",defaultValue = "10")Integer limit,
                                ServletRequest request){
        Map map = WebUtils.getParametersStartingWith(request, "s_");
        PageData<Role> roleLayerData = new PageData<>();
        QueryWrapper<Role> roleWrapper = new QueryWrapper<>();
        roleWrapper.eq("del_flag",false);
        if(!map.isEmpty()){
            String keys = (String) map.get("key");
            if(StringUtils.isNotBlank(keys)) {
                roleWrapper.like("name", keys);
            }
        }
        IPage<Role> rolePage = roleService.page(new Page<>(page,limit),roleWrapper);
        roleLayerData.setCount(rolePage.getTotal());
        roleLayerData.setData(setUserToRole(rolePage.getRecords()));
        return roleLayerData;
    }

    private List<Role> setUserToRole(List<Role> roles){
        roles.forEach(r -> {
            if(StringUtils.isNotBlank(r.getCreateId())){
                User u = userService.findUserById(r.getCreateId());
                if(StringUtils.isBlank(u.getNickName())){
                    u.setNickName(u.getLoginName());
                }
                r.setCreateUser(u);
            }
            if(StringUtils.isNotBlank(r.getUpdateId())){
                User u  = userService.findUserById(r.getUpdateId());
                if(StringUtils.isBlank(u.getNickName())){
                    u.setNickName(u.getLoginName());
                }
                r.setUpdateUser(u);
            }
        });

        return roles;
    }

    @GetMapping("add")
    public String add(ModelMap modelMap){
        Map<String,Object> map =  new HashMap();
        map.put("parentId",null);
        map.put("isShow",false);
        List<Menu> menuList = menuService.selectAllMenus(map);
        modelMap.put("menuList",menuList);
        return "admin/system/role/add";
    }

    @RequiresPermissions("sys:role:add")
    @PostMapping("add")
    @ResponseBody
    @SysLog("保存新增角色数据")
    public ResponseEntity add(@RequestBody Role role){
        if(StringUtils.isBlank(role.getName())){
            return ResponseEntity.failure("角色名称不能为空");
        }
        if(roleService.getRoleNameCount(role.getName())>0){
            return ResponseEntity.failure("角色名称已存在");
        }
        roleService.saveRole(role);
        return ResponseEntity.success("操作成功");
    }

    @GetMapping("edit")
    public String edit(String id,ModelMap modelMap){
        Role role = roleService.getRoleById(id);
        StringBuilder menuIds = new StringBuilder();
        if(role != null) {
            Set<Menu> menuSet = role.getMenuSet();
            if (menuSet != null && menuSet.size() > 0) {
                for (Menu m : menuSet) {
                    menuIds.append(m.getId().toString()).append(",");
                }
            }
        }
        Map<String,Object> map = new HashMap();
        map.put("parentId",null);
        map.put("isShow",Boolean.FALSE);
        List<Menu> menuList = menuService.selectAllMenus(map);
        modelMap.put("role",role);
        modelMap.put("menuList",menuList);
        modelMap.put("menuIds",menuIds.toString());
        return "admin/system/role/edit";
    }

    @RequiresPermissions("sys:role:edit")
    @PostMapping("edit")
    @ResponseBody
    @SysLog("保存编辑角色数据")
    public ResponseEntity edit(@RequestBody Role role){
        if(StringUtils.isNotBlank(role.getId())) {
            return ResponseEntity.failure("角色ID不能为空");
        }
        if(StringUtils.isBlank(role.getName())){
            return ResponseEntity.failure("角色名称不能为空");
        }
        Role oldRole = roleService.getRoleById(role.getId());
        if(!oldRole.getName().equals(role.getName())){
            if(roleService.getRoleNameCount(role.getName())>0){
                return ResponseEntity.failure("角色名称已存在");
            }
        }
        roleService.updateRole(role);
        return ResponseEntity.success("操作成功");
    }

    @RequiresPermissions("sys:role:delete")
    @PostMapping("delete")
    @ResponseBody
    @SysLog("删除角色数据")
    public ResponseEntity delete(@RequestParam(value = "id",required = false)String id){
        if(StringUtils.isNotBlank(id)){
            return ResponseEntity.failure("角色ID不能为空");
        }
        Role role = roleService.getRoleById(id);
        roleService.deleteRole(role);
        return ResponseEntity.success("操作成功");
    }

    @RequiresPermissions("sys:role:delete")
    @PostMapping("deleteSome")
    @ResponseBody
    @SysLog("多选删除角色数据")
    public ResponseEntity deleteSome(@RequestBody List<Role> roles){
        if(roles == null || roles.size()==0){
            return ResponseEntity.failure("请选择需要删除的角色");
        }
        for (Role r : roles){
            roleService.deleteRole(r);
        }
        return ResponseEntity.success("操作成功");
    }
}
