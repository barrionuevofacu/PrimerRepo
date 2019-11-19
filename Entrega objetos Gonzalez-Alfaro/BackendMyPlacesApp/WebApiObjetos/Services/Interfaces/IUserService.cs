using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using WebApiObjetos.Domain;
using WebApiObjetos.Models.Entities;

namespace WebApiObjetos.Services.Interfaces
{
    public interface IUserService
    {
        Task<UserDTO> Login(UserDTO user);

        Task<bool> SignIn(UserDTO user);

    }
}
