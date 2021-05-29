/*
 * Copyright (C) 2016 attila
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.emraxxor.web.ide.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.github.emraxxor.web.ide.entities.User;


/**
 *
 * Repository interface for {@link User} instances. Provides basic CRUD
 * operations due to the extension of {@link JpaRepository}.
 *
 *
 * @author attila
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByNeptunId(String neptunId);
    
    Optional<User> findByUserMail(String mail);
    
    Optional<User> findByNeptunIdIgnoreCase(String neptunId);

    Page<User> findAll(Pageable pageable);
    
    List<User> removeByUserId(Long uid);
}
