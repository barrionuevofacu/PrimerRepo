using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Domain;

namespace WebApiObjetos.Models.Entities
{
    public class Image
    {
        public int Id { get; set; }

        public String Picture { get; set; }

        public int UserId { get; set; }

        public ImageDTO toDto()
        {
            return new ImageDTO
            {
                Id = this.Id,
                Picture = this.Picture,
                UserId = this.UserId
            };
        }
    }
}
